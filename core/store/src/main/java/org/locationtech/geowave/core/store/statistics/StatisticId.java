package org.locationtech.geowave.core.store.statistics;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.StringUtils;
import org.locationtech.geowave.core.store.api.StatisticValue;
import com.google.common.primitives.Bytes;

public class StatisticId<V extends StatisticValue<?>> {

  public static final byte[] UNIQUE_ID_SEPARATOR = new byte[] {'|'};

  protected final ByteArray groupId;

  protected final StatisticType<V> statisticType;

  protected final String tag;

  protected ByteArray cachedBytes = null;

  public StatisticId(
      final ByteArray groupId,
      final StatisticType<V> statisticType,
      final String tag) {
    this.groupId = groupId;
    this.statisticType = statisticType;
    this.tag = tag;
  }

  public StatisticType<V> getStatisticType() {
    return statisticType;
  }

  public String getTag() {
    return tag;
  }

  public ByteArray getGroupId() {
    return groupId;
  }

  public ByteArray getUniqueId() {
    if (cachedBytes == null) {
      cachedBytes = generateUniqueId(statisticType, tag);
    }
    return cachedBytes;
  }

  public static ByteArray generateUniqueId(final StatisticType<?> statisticType, final String tag) {
    if (tag == null) {
      return statisticType;
    } else {
      return new ByteArray(
          Bytes.concat(
              statisticType.getBytes(),
              UNIQUE_ID_SEPARATOR,
              StringUtils.stringToBinary(tag)));
    }
  }

}
