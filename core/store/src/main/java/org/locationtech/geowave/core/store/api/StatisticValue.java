package org.locationtech.geowave.core.store.api;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import com.google.common.primitives.Bytes;

public abstract class StatisticValue<R> {
  public static final ByteArray NO_BIN = new ByteArray();
  private final Statistic<?> statistic;

  private ByteArray bin = NO_BIN;

  public StatisticValue(final Statistic<?> statistic) {
    this.statistic = statistic;
  }

  public Statistic<?> getStatistic() {
    return statistic;
  }

  /**
   * Sets the bin for this value.
   * 
   * NOTE: This is only used when querying values from multiple bins so that the query issuer can
   * know which bin a particular value belongs to.
   * 
   * @param bin the bin for this value
   */
  public void setBin(final ByteArray bin) {
    this.bin = bin;
  }

  /**
   * Gets the bin for this value.
   * 
   * NOTE: This is only used when querying values from multiple bins so that the query issuer can
   * know which bin a particular value belongs to.
   * 
   * @return the bin for this value
   */
  public ByteArray getBin() {
    return bin;
  }

  public abstract R getValue();

  public abstract byte[] toBinary();

  public abstract void fromBinary(byte[] bytes);

  public abstract void merge(StatisticValue<R> merge);

  public static byte[] getValueId(StatisticId<?> statisticId, ByteArray bin) {
    if (bin != null) {
      return Bytes.concat(
          statisticId.getUniqueId().getBytes(),
          StatisticId.UNIQUE_ID_SEPARATOR,
          bin.getBytes());
    }
    return statisticId.getUniqueId().getBytes();
  }
}
