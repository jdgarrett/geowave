package org.locationtech.geowave.core.store.api;

import org.locationtech.geowave.core.store.entities.GeoWaveRow;

public interface StatisticBinningStrategy {

  public <T> byte[][] getBins(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows);

}
