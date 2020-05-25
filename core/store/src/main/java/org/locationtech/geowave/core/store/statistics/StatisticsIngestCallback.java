package org.locationtech.geowave.core.store.statistics;

import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;

public interface StatisticsIngestCallback {

  public <T> void entryIngested(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows);

}
