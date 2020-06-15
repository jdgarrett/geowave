package org.locationtech.geowave.core.store.api;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.statistics.StatisticType;

/**
 * Base interface for statistic queries.
 *
 * @param <V> the statistic value type
 * @param <R> the return type of the statistic value
 */
public interface StatisticQuery<V extends StatisticValue<R>, R> {
  /**
   * @return the statistic type for the query
   */
  public StatisticType<V> statisticType();

  /**
   * @return the tag filter
   */
  public String tag();

  /**
   * @return the bin filter
   */
  public ByteArray[] bins();

  /**
   * @return the authorizations for the query
   */
  public String[] authorizations();
}
