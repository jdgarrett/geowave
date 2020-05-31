package org.locationtech.geowave.adapter.vector.plugin.transaction;

import java.util.Map;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.DataStatisticsStore;
import com.beust.jcommander.internal.Maps;

public class StatisticsCache {
  
  private Map<Statistic<?>, StatisticValue<?>> indexStatistics = Maps.newHashMap();
  
  public StatisticsCache(final DataStatisticsStore statisticsStore, final DataTypeAdapter<?> adapter, String... authorizations) {
    // Pull out any statistics that WE NEED (not everything)
    // INDEX
    
    // ADAPTER
    // Count
    // BoundingBox
    
    // FIELD
    // FeatureTimeRangeStatistics
    // NumericRagneStatistics
  }

}
