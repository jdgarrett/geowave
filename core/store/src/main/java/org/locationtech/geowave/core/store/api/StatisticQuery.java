package org.locationtech.geowave.core.store.api;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.statistics.StatisticType;

public interface StatisticQuery<V extends StatisticValue<R>, R> {
  public StatisticType<V> statisticType();

  public String tag();

  public ByteArray[] bins();

  public String[] authorizations();
}
