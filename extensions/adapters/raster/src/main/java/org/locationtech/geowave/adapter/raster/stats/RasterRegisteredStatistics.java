package org.locationtech.geowave.adapter.raster.stats;

import org.locationtech.geowave.core.store.statistics.StatisticsRegistrySPI;

public class RasterRegisteredStatistics implements StatisticsRegistrySPI {

  @Override
  public RegisteredStatistic[] getProvidedStatistics() {
    return new RegisteredStatistic[] {
        // Adapter Statistics
        new RegisteredStatistic(
            RasterBoundingBoxStatistic.STATS_TYPE,
            RasterBoundingBoxStatistic::new),
        new RegisteredStatistic(RasterFootprintStatistic.STATS_TYPE, RasterFootprintStatistic::new),
        new RegisteredStatistic(RasterHistogramStatistic.STATS_TYPE, RasterHistogramStatistic::new),
        new RegisteredStatistic(RasterOverviewStatistic.STATS_TYPE, RasterOverviewStatistic::new)};
  }

  @Override
  public RegisteredBinningStrategy[] getProvidedBinningStrategies() {
    return new RegisteredBinningStrategy[] {};
  }

}
