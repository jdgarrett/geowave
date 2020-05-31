package org.locationtech.geowave.core.geotime.store.statistics;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.locationtech.geowave.core.geotime.store.query.TemporalRange;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.locationtech.geowave.core.store.statistics.BaseStatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticsIngestCallback;
import org.threeten.extra.Interval;

public abstract class TimeRangeStatisticValue extends BaseStatisticValue<Interval> implements StatisticsIngestCallback {
  private long min = Long.MAX_VALUE;
  private long max = Long.MIN_VALUE;

  public boolean isSet() {
    if ((min == Long.MAX_VALUE) && (max == Long.MIN_VALUE)) {
      return false;
    }
    return true;
  }

  public TemporalRange asTemporalRange() {
    return new TemporalRange(new Date(getMin()), new Date(getMax()));
  }

  public long getMin() {
    return min;
  }

  public long getMax() {
    return max;
  }

  public long getRange() {
    return max - min;
  }
  
  public Date getMaxTime() {
    final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    c.setTimeInMillis(getMax());
    return c.getTime();
  }

  public Date getMinTime() {
    final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    c.setTimeInMillis(getMin());
    return c.getTime();
  }

  @Override
  public byte[] toBinary() {
    final ByteBuffer buffer = ByteBuffer.allocate(VarintUtils.timeByteLength(min) + VarintUtils.timeByteLength(max));
    VarintUtils.writeTime(min, buffer);
    VarintUtils.writeTime(max, buffer);
    return buffer.array();
  }

  @Override
  public void fromBinary(final byte[] bytes) {
    final ByteBuffer buffer = ByteBuffer.wrap(bytes);
    min = VarintUtils.readTime(buffer);
    max = VarintUtils.readTime(buffer);
  }

  @Override
  public <T> void entryIngested(final DataTypeAdapter<T> adapter, final T entry, final GeoWaveRow... rows) {
    final Interval range = getInterval(adapter, entry, rows);
    if (range != null) {
      min = Math.min(min, range.getStart().toEpochMilli());
      max = Math.max(max, range.getEnd().toEpochMilli());
    }
  }

  protected abstract <T> Interval getInterval(final DataTypeAdapter<T> adapter, final T entry, final GeoWaveRow... rows);

  @Override
  public void merge(final StatisticValue<Interval> merge) {
    if ((merge != null) && (merge instanceof TimeRangeStatisticValue)) {
      final TimeRangeStatisticValue stats = (TimeRangeStatisticValue) merge;
      if (stats.isSet()) {
        min = Math.min(min, stats.getMin());
        max = Math.max(max, stats.getMax());
      }
    }
  }

  @Override
  public Interval getValue() {
    if (isSet()) {
      return Interval.of(Instant.ofEpochMilli(min), Instant.ofEpochMilli(max));
    }
    return null;
  }
}
