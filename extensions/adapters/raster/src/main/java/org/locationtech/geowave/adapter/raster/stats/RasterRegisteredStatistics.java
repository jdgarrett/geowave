package org.locationtech.geowave.adapter.raster.stats;

import org.locationtech.geowave.adapter.raster.stats.RasterBoundingBoxStatistic.RasterBoundingBoxValue;
import org.locationtech.geowave.adapter.raster.stats.RasterFootprintStatistic.RasterFootprintValue;
import org.locationtech.geowave.adapter.raster.stats.RasterHistogramStatistic.RasterHistogramValue;
import org.locationtech.geowave.adapter.raster.stats.RasterOverviewStatistic.RasterOverviewValue;
import org.locationtech.geowave.core.store.statistics.StatisticsRegistrySPI;

public class RasterRegisteredStatistics extends StatisticsRegistrySPI {

  @Override
  public RegisteredStatistic[] getProvidedStatistics() {
    return new RegisteredStatistic[] {
        // Adapter Statistics
        new RegisteredStatistic(
            RasterBoundingBoxStatistic.STATS_TYPE,
            RasterBoundingBoxStatistic::new,
            RasterBoundingBoxValue::new,
            (short) 2300,
            (short) 2301),
        new RegisteredStatistic(
            RasterFootprintStatistic.STATS_TYPE,
            RasterFootprintStatistic::new,
            RasterFootprintValue::new,
            (short) 2302,
            (short) 2303),
        new RegisteredStatistic(
            RasterHistogramStatistic.STATS_TYPE,
            RasterHistogramStatistic::new,
            RasterHistogramValue::new,
            (short) 2304,
            (short) 2305),
        new RegisteredStatistic(
            RasterOverviewStatistic.STATS_TYPE,
            RasterOverviewStatistic::new,
            RasterOverviewValue::new,
            (short) 2306,
            (short) 2307)};
  }

  @Override
  public RegisteredBinningStrategy[] getProvidedBinningStrategies() {
    return new RegisteredBinningStrategy[] {};
  }

}
