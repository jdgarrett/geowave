package org.locationtech.geowave.core.store.statistics.query;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticType;

public class IndexStatisticQuery<V extends StatisticValue<R>, R> extends
    AbstractStatisticQuery<V, R> {

  private final String indexName;

  public IndexStatisticQuery(
      final StatisticType<V> statisticType,
      final String indexName,
      final String tag,
      final ByteArray[] bins,
      final String[] authorizations) {
    super(statisticType, tag, bins, authorizations);
    this.indexName = indexName;
  }

  public String indexName() {
    return indexName;
  }

}
