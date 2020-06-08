package org.locationtech.geowave.core.geotime.store.statistics;

import org.locationtech.geowave.core.store.statistics.StatisticsRegistrySPI;

public class GeotimeRegisteredStatistics implements StatisticsRegistrySPI {

  @Override
  public RegisteredStatistic[] getProvidedStatistics() {
    return new RegisteredStatistic[] {
        // Field Statistics
        new RegisteredStatistic(BoundingBoxStatistic.STATS_TYPE, BoundingBoxStatistic::new),
        new RegisteredStatistic(TimeRangeStatistic.STATS_TYPE, TimeRangeStatistic::new)};
  }

  @Override
  public RegisteredBinningStrategy[] getProvidedBinningStrategies() {
    return new RegisteredBinningStrategy[] {};
  }
}
