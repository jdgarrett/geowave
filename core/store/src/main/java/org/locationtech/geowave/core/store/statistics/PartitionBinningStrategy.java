package org.locationtech.geowave.core.store.statistics;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.StatisticBinningStrategy;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;

public class PartitionBinningStrategy implements StatisticBinningStrategy {

  @Override
  public <T> ByteArray[] getBins(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows) {
    ByteArray[] partitionKeys = new ByteArray[rows.length];
    for (int i = 0; i < rows.length; i++) {
      partitionKeys[i] = getBin(rows[i].getPartitionKey());
    }
    return partitionKeys;
  }

  public static ByteArray getBin(final byte[] partitionKey) {
    if (partitionKey == null) {
      return new ByteArray();
    }
    return new ByteArray(partitionKey);
  }

  @Override
  public byte[] toBinary() {
    return new byte[0];
  }

  @Override
  public void fromBinary(byte[] bytes) {}

}