package org.locationtech.geowave.core.store.statistics.query;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.field.FieldStatisticType;

public class FieldStatisticQueryBuilder<V extends StatisticValue<R>, R> extends
    AbstractStatisticQueryBuilder<V, R, FieldStatisticQueryBuilder<V, R>> {

  protected String typeName = null;

  protected String fieldName = null;

  public FieldStatisticQueryBuilder() {}

  public FieldStatisticQueryBuilder(final FieldStatisticType<V> type) {
    statisticType(type);
  }

  public FieldStatisticQueryBuilder<V, R> typeName(final String typeName) {
    this.typeName = typeName;
    return this;
  }

  public FieldStatisticQueryBuilder<V, R> fieldName(final String fieldName) {
    this.fieldName = fieldName;
    return this;
  }

  @Override
  public AbstractStatisticQuery<V, R> build() {
    ByteArray[] binArray = bins.toArray(new ByteArray[bins.size()]);
    String[] authorizationsArray = authorizations.toArray(new String[authorizations.size()]);
    return new FieldStatisticQuery<V, R>(
        statisticType,
        typeName,
        fieldName,
        tag,
        binArray,
        authorizationsArray);
  }
}
