package org.locationtech.geowave.core.store.api;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import com.google.common.primitives.Bytes;

public abstract class StatisticValue<R> {
  private final Statistic<?> statistic;
  
  public StatisticValue(final Statistic<?> statistic) {
    this.statistic = statistic;
  }
  
  public Statistic<?> getStatistic() {
    return statistic;
  }
  
  public abstract R getValue();

  public abstract byte[] toBinary();

  public abstract void fromBinary(byte[] bytes);
  
  public abstract void merge(StatisticValue<R> merge);
  
  public static byte[] getValueId(StatisticId<?> statisticId, ByteArray bin) {
    if (bin != null) {
      return Bytes.concat(statisticId.getUniqueId().getBytes(), StatisticId.UNIQUE_ID_SEPARATOR, bin.getBytes());
    }
    return statisticId.getUniqueId().getBytes();
  }
}
