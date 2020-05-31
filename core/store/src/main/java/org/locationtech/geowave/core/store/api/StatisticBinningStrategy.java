package org.locationtech.geowave.core.store.api;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import com.google.common.primitives.Bytes;

public interface StatisticBinningStrategy {

  public <T> ByteArray[] getBins(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows);
  
  public static byte[] getBinnedValueId(StatisticId<?> statisticId, ByteArray bin) {
    return Bytes.concat(statisticId.getUniqueId().getBytes(), StatisticId.UNIQUE_ID_SEPARATOR, bin.getBytes());
  }

}
