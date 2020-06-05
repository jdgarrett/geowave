package org.locationtech.geowave.core.store.statistics.query;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.adapter.AdapterStatisticType;

public class AdapterStatisticQueryBuilder<V extends StatisticValue<R>, R> extends
    AbstractStatisticQueryBuilder<V, R, AdapterStatisticQueryBuilder<V, R>> {

  protected String typeName = null;

  public AdapterStatisticQueryBuilder(final AdapterStatisticType<V> type) {
    statisticType(type);
  }

  public AdapterStatisticQueryBuilder<V, R> typeName(final String typeName) {
    this.typeName = typeName;
    return this;
  }

  @Override
  public AbstractStatisticQuery<V, R> build() {
    ByteArray[] binArray = bins.toArray(new ByteArray[bins.size()]);
    String[] authorizationsArray = authorizations.toArray(new String[authorizations.size()]);
    return new AdapterStatisticQuery<V, R>(
        statisticType,
        typeName,
        tag,
        binArray,
        authorizationsArray);
  }
}
