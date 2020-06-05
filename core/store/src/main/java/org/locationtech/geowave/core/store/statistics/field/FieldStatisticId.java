package org.locationtech.geowave.core.store.statistics.field;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.StringUtils;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import com.google.common.primitives.Bytes;

public class FieldStatisticId<V extends StatisticValue<?>> extends StatisticId<V> {

  private final String fieldName;

  public FieldStatisticId(
      final ByteArray groupId,
      final StatisticType<V> statisticType,
      final String fieldName,
      final String tag) {
    super(groupId, statisticType, tag);
    this.fieldName = fieldName;
  }

  public String getFieldName() {
    return fieldName;
  }

  @Override
  public ByteArray getUniqueId() {
    if (cachedBytes == null) {
      cachedBytes = generateUniqueId(statisticType, fieldName, tag);
    }
    return cachedBytes;
  }

  public static ByteArray generateUniqueId(
      final StatisticType<?> statisticType,
      final String fieldName,
      final String tag) {
    if (tag == null) {
      return new ByteArray(
          Bytes.concat(
              statisticType.getBytes(),
              StatisticId.UNIQUE_ID_SEPARATOR,
              StringUtils.stringToBinary(fieldName)));
    } else {
      return new ByteArray(
          Bytes.concat(
              statisticType.getBytes(),
              StatisticId.UNIQUE_ID_SEPARATOR,
              StringUtils.stringToBinary(fieldName),
              StatisticId.UNIQUE_ID_SEPARATOR,
              StringUtils.stringToBinary(tag)));
    }
  }
}
