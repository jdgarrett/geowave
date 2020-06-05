package org.locationtech.geowave.core.store.api;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.persist.Persistable;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import com.google.common.primitives.Bytes;

public interface StatisticBinningStrategy extends Persistable {

  public <T> ByteArray[] getBins(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows);

  public static byte[] getBinnedValueId(StatisticId<?> statisticId, ByteArray bin) {
    return Bytes.concat(
        statisticId.getUniqueId().getBytes(),
        StatisticId.UNIQUE_ID_SEPARATOR,
        bin.getBytes());
  }

  public static ByteArray getBinFromValueId(
      final StatisticId<?> statisticId,
      final byte[] valueId) {
    int binIndex =
        statisticId.getUniqueId().getBytes().length + StatisticId.UNIQUE_ID_SEPARATOR.length;
    byte[] binBytes = new byte[valueId.length - binIndex];
    for (int i = 0; i < binBytes.length; i++) {
      binBytes[i] = valueId[i + binIndex];
    }
    return new ByteArray(binBytes);
  }

}
