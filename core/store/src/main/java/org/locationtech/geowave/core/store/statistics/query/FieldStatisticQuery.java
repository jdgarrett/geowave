package org.locationtech.geowave.core.store.statistics.query;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticType;

public class FieldStatisticQuery<V extends StatisticValue<R>, R> extends
    AbstractStatisticQuery<V, R> {

  private final String typeName;
  private final String fieldName;

  public FieldStatisticQuery(
      final StatisticType<V> statisticType,
      final String typeName,
      final String fieldName,
      final String tag,
      final ByteArray[] bins,
      final String[] authorizations) {
    super(statisticType, tag, bins, authorizations);
    this.typeName = typeName;
    this.fieldName = fieldName;
  }

  public String typeName() {
    return typeName;
  }

  public String fieldName() {
    return fieldName;
  }

}
