package org.locationtech.geowave.core.store.statistics;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.StatisticValue;

public class BinnedStatisticValue<R> extends StatisticValue<R> {
  
  private final StatisticValue<R> value;
  private final ByteArray bin;
  
  public BinnedStatisticValue(final StatisticValue<R> value, final ByteArray bin) {
    super(value.getStatistic());
    this.value = value;
    this.bin = bin;
  }

  public ByteArray getBin() {
    return bin;
  }

  @Override
  public R getValue() {
    return value.getValue();
  }

  @Override
  public byte[] toBinary() {
    return value.toBinary();
  }

  @Override
  public void fromBinary(byte[] bytes) {
    value.fromBinary(bytes);
  }

  @Override
  public void merge(StatisticValue<R> merge) {
    value.merge(merge);
  }

}
