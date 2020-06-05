package org.locationtech.geowave.core.store.statistics.query;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.index.IndexStatisticType;

public class IndexStatisticQueryBuilder<V extends StatisticValue<R>, R> extends
    AbstractStatisticQueryBuilder<V, R, IndexStatisticQueryBuilder<V, R>> {

  protected String indexName = null;

  public IndexStatisticQueryBuilder(final IndexStatisticType<V> type) {
    statisticType(type);
  }

  public IndexStatisticQueryBuilder<V, R> indexName(final String indexName) {
    this.indexName = indexName;
    return this;
  }

  @Override
  public AbstractStatisticQuery<V, R> build() {
    ByteArray[] binArray = bins.toArray(new ByteArray[bins.size()]);
    String[] authorizationsArray = authorizations.toArray(new String[authorizations.size()]);
    return new IndexStatisticQuery<V, R>(
        statisticType,
        indexName,
        tag,
        binArray,
        authorizationsArray);
  }
}
