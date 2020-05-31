package org.locationtech.geowave.core.store.statistics.query;

import org.locationtech.geowave.core.store.api.StatisticValue;

public class IndexStatisticQueryBuilder<V extends StatisticValue<R>, R> extends StatisticQueryBuilder<V, R, IndexStatisticQueryBuilder<V, R>>{
  
  protected String indexName = null;
  
  public IndexStatisticQueryBuilder<V, R> setIndex(final String indexName) {
    this.indexName = indexName;
    return this;
  }

  @Override
  public StatisticsQuery<V, R> build() {
    return new IndexStatisticQuery<V, R>();
  }
}
