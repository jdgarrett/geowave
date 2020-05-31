package org.locationtech.geowave.core.store.statistics;

import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.entities.GeoWaveMetadata;
import org.locationtech.geowave.core.store.operations.MetadataWriter;
import com.google.common.primitives.Bytes;

public class StatisticValueWriter<V extends StatisticValue<?>> implements AutoCloseable {
  private final MetadataWriter writer;
  private final Statistic<V> statistic;

  public StatisticValueWriter(final MetadataWriter writer, final Statistic<V> statistic) {
    this.writer = writer;
    this.statistic = statistic;
  }

  @Override
  public void close() throws Exception {
    writer.close();
  }

  public void writeStatisticValue(final byte[] bin, final byte[] visibility, V value) {
    writer.write(
        new GeoWaveMetadata(
            Bytes.concat(
                statistic.getId().getUniqueId().getBytes(),
                StatisticId.UNIQUE_ID_SEPARATOR,
                bin),
            statistic.getId().getGroupId().getBytes(),
            visibility,
            value.toBinary()));
  }

}
