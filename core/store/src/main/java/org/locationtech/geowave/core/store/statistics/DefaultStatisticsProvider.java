package org.locationtech.geowave.core.store.statistics;

import java.util.List;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;

public interface DefaultStatisticsProvider {
  public List<Statistic<? extends StatisticValue<?>>> getDefaultStatistics();
}
