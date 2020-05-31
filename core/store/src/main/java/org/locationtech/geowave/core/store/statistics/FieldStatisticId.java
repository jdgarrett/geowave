package org.locationtech.geowave.core.store.statistics;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.StatisticValue;
import com.google.common.primitives.Bytes;

public class FieldStatisticId<V extends StatisticValue<?>> extends StatisticId<V> {
  
  private final String fieldName;
  
  public FieldStatisticId(final ByteArray groupId, final StatisticType<V> statisticType, final String fieldName, final String name) {
    super(groupId, statisticType, name);
    this.fieldName = fieldName;
  }
  
  public String getFieldName() {
    return fieldName;
  }
  
  @Override
  public ByteArray getUniqueId() {
    if (cachedBytes == null) {
      cachedBytes = generateUniqueId(statisticType, fieldName, name);
    }
    return cachedBytes;
  }
  
  public static ByteArray generateUniqueId(
      final StatisticType<?> statisticType,
      final String fieldName,
      final String name) {
    if (name == null) {
      return new ByteArray(
          Bytes.concat(
              statisticType.getBytes(),
              StatisticId.UNIQUE_ID_SEPARATOR,
              fieldName.getBytes()));
    } else {
      return new ByteArray(
          Bytes.concat(
              statisticType.getBytes(),
              StatisticId.UNIQUE_ID_SEPARATOR,
              fieldName.getBytes(),
              StatisticId.UNIQUE_ID_SEPARATOR,
              name.getBytes()));
    }
  }
}
