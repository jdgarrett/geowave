package org.locationtech.geowave.core.store.api;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.Mergeable;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import com.google.common.primitives.Bytes;

public abstract class StatisticValue<R> implements Mergeable {
  public static final ByteArray NO_BIN = new ByteArray();
  protected final Statistic<?> statistic;

  protected ByteArray bin = NO_BIN;

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

  /**
   * Merge another statistic value into this one.
   * 
   * IMPORTANT: This function cannot guarantee that the Statistic will be available. Any variables
   * needed from the statistic for merging must be serialized with the value.
   */
  @Override
  public abstract void merge(Mergeable merge);

  public abstract R getValue();

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
