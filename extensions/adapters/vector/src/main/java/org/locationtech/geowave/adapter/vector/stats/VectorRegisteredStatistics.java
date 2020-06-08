package org.locationtech.geowave.adapter.vector.stats;

import org.locationtech.geowave.core.store.statistics.StatisticsRegistrySPI;

public class VectorRegisteredStatistics implements StatisticsRegistrySPI {

  @Override
  public RegisteredStatistic[] getProvidedStatistics() {
    return new RegisteredStatistic[] {
        // Field Statistics
        new RegisteredStatistic(CountMinSketchStatistic.STATS_TYPE, CountMinSketchStatistic::new),
        new RegisteredStatistic(HyperLogLogStatistic.STATS_TYPE, HyperLogLogStatistic::new),
        new RegisteredStatistic(
            NumericHistogramStatistic.STATS_TYPE,
            NumericHistogramStatistic::new)};
  }

  @Override
  public RegisteredBinningStrategy[] getProvidedBinningStrategies() {
    return new RegisteredBinningStrategy[] {};
  }

}
