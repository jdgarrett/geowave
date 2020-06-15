package org.locationtech.geowave.core.store.statistics.query;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.adapter.DataTypeStatisticType;

public class DataTypeStatisticQueryBuilder<V extends StatisticValue<R>, R> extends
    AbstractStatisticQueryBuilder<V, R, DataTypeStatisticQueryBuilder<V, R>> {

  protected String typeName = null;

  public DataTypeStatisticQueryBuilder(final DataTypeStatisticType<V> type) {
    super(type);
  }

  public DataTypeStatisticQueryBuilder<V, R> typeName(final String typeName) {
    this.typeName = typeName;
    return this;
  }

  @Override
  public AbstractStatisticQuery<V, R> build() {
    ByteArray[] binArray = bins.toArray(new ByteArray[bins.size()]);
    String[] authorizationsArray = authorizations.toArray(new String[authorizations.size()]);
    return new DataTypeStatisticQuery<V, R>(
        statisticType,
        typeName,
        tag,
        binArray,
        authorizationsArray);
  }
}
