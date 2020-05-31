package org.locationtech.geowave.core.store.statistics;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.StatisticBinningStrategy;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;

public class AdapterBinningStrategy implements StatisticBinningStrategy {

  @Override
  public <T> ByteArray[] getBins(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows) {
    return new ByteArray[] { getBin(adapter) };
  }
  
  public static ByteArray getBin(DataTypeAdapter<?> adapter) {
    return new ByteArray(adapter.getTypeName().getBytes());
  }

}
