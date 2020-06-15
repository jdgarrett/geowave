package org.locationtech.geowave.core.store.statistics.query;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticType;

public class DataTypeStatisticQuery<V extends StatisticValue<R>, R> extends
    AbstractStatisticQuery<V, R> {

  private final String typeName;

  public DataTypeStatisticQuery(
      final StatisticType<V> statisticType,
      final String typeName,
      final String tag,
      final ByteArray[] bins,
      final String[] authorizations) {
    super(statisticType, tag, bins, authorizations);
    this.typeName = typeName;
  }

  public String typeName() {
    return typeName;
  }
}
