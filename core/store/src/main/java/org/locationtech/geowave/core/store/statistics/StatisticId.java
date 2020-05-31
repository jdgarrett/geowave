package org.locationtech.geowave.core.store.statistics;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.StatisticValue;
import com.google.common.primitives.Bytes;

public class StatisticId<V extends StatisticValue<?>> {
  
  public static final byte[] UNIQUE_ID_SEPARATOR = "|".getBytes();
  
  protected final ByteArray groupId;
  
  protected final StatisticType<V> statisticType;
  
  protected final String name;
  
  protected ByteArray cachedBytes = null;
  
  public StatisticId(final ByteArray groupId, final StatisticType<V> statisticType, final String name) {
    this.groupId = groupId;
    this.statisticType = statisticType;
    this.name = name;
  }
  
  public StatisticType<V> getStatisticType() {
    return statisticType;
  }
  
  public String getName() {
    return name;
  }
  
  public ByteArray getGroupId() {
    return groupId;
  }
  
  public ByteArray getUniqueId() {
    if (cachedBytes == null) {
      cachedBytes = generateUniqueId(statisticType, name);
    }
    return cachedBytes;
  }
  
  public static ByteArray generateUniqueId(
      final StatisticType<?> statisticType,
      final String name) {
    if (name == null) {
      return statisticType;
    } else {
      return new ByteArray(
          Bytes.concat(
              statisticType.getBytes(),
              UNIQUE_ID_SEPARATOR,
              name.getBytes()));
    }
  }

}
