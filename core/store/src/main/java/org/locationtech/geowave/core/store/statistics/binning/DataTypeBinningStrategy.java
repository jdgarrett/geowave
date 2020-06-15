package org.locationtech.geowave.core.store.statistics.binning;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.StatisticBinningStrategy;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;

public class DataTypeBinningStrategy implements StatisticBinningStrategy {
  public static final String NAME = "DATA_TYPE";

  @Override
  public String getStrategyName() {
    return NAME;
  }

  @Override
  public String getDescription() {
    return "Bin the statistic by data type.  Only used for index statistics.";
  }

  @Override
  public <T> ByteArray[] getBins(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows) {
    return new ByteArray[] {getBin(adapter)};
  }

  public static ByteArray getBin(DataTypeAdapter<?> adapter) {
    if (adapter == null) {
      return new ByteArray();
    }
    return new ByteArray(adapter.getTypeName());
  }

  public static ByteArray getBin(final String typeName) {
    if (typeName == null) {
      return new ByteArray();
    }
    return new ByteArray(typeName);
  }

  @Override
  public byte[] toBinary() {
    return new byte[0];
  }

  @Override
  public void fromBinary(byte[] bytes) {}

  @Override
  public String binToString(final ByteArray bin) {
    return bin.getString();
  }

}
