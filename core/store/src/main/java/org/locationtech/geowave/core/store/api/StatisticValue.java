package org.locationtech.geowave.core.store.api;

import org.locationtech.geowave.core.index.Mergeable;

public interface StatisticValue<R> extends Mergeable {

  public R getValue();

  public byte[] toBinary();

  public void fromBinary(byte[] bytes);

}
