package org.locationtech.geowave.core.geotime.store.statistics;

import org.locationtech.geowave.core.geotime.store.statistics.BoundingBoxStatistic.BoundingBoxValue;
import org.locationtech.geowave.core.geotime.store.statistics.TimeRangeStatistic.TimeRangeValue;
import org.locationtech.geowave.core.store.statistics.StatisticsRegistrySPI;

public class GeotimeRegisteredStatistics extends StatisticsRegistrySPI {

  @Override
  public RegisteredStatistic[] getProvidedStatistics() {
    return new RegisteredStatistic[] {
        // Field Statistics
        new RegisteredStatistic(
            BoundingBoxStatistic.STATS_TYPE,
            BoundingBoxStatistic::new,
            BoundingBoxValue::new,
            (short) 2100,
            (short) 2101),
        new RegisteredStatistic(
            TimeRangeStatistic.STATS_TYPE,
            TimeRangeStatistic::new,
            TimeRangeValue::new,
            (short) 2102,
            (short) 2103)};
  }

  @Override
  public RegisteredBinningStrategy[] getProvidedBinningStrategies() {
    return new RegisteredBinningStrategy[] {};
  }
}
