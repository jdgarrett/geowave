package org.locationtech.geowave.adapter.vector.stats;

import org.locationtech.geowave.adapter.vector.stats.CountMinSketchStatistic.CountMinSketchValue;
import org.locationtech.geowave.adapter.vector.stats.HyperLogLogStatistic.HyperLogLogPlusValue;
import org.locationtech.geowave.adapter.vector.stats.NumericHistogramStatistic.NumericHistogramValue;
import org.locationtech.geowave.core.store.statistics.StatisticsRegistrySPI;

public class VectorRegisteredStatistics extends StatisticsRegistrySPI {

  @Override
  public RegisteredStatistic[] getProvidedStatistics() {
    return new RegisteredStatistic[] {
        // Field Statistics
        new RegisteredStatistic(
            CountMinSketchStatistic.STATS_TYPE,
            CountMinSketchStatistic::new,
            CountMinSketchValue::new,
            (short) 2200,
            (short) 2201),
        new RegisteredStatistic(
            HyperLogLogStatistic.STATS_TYPE,
            HyperLogLogStatistic::new,
            HyperLogLogPlusValue::new,
            (short) 2202,
            (short) 2203),
        new RegisteredStatistic(
            NumericHistogramStatistic.STATS_TYPE,
            NumericHistogramStatistic::new,
            NumericHistogramValue::new,
            (short) 2204,
            (short) 2205)};
  }

  @Override
  public RegisteredBinningStrategy[] getProvidedBinningStrategies() {
    return new RegisteredBinningStrategy[] {};
  }

}
