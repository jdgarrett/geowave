package org.locationtech.geowave.adapter.raster.stats;

import org.locationtech.geowave.adapter.raster.stats.RasterBoundingBoxStatistic.RasterBoundingBoxValue;
import org.locationtech.geowave.core.geotime.store.statistics.BoundingBoxStatistic;
import org.locationtech.geowave.core.geotime.store.statistics.BoundingBoxStatistic.BoundingBoxValue;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.statistics.DataStatisticsStore;
import org.locationtech.geowave.core.store.statistics.adapter.AdapterStatistic;
import org.locationtech.geowave.core.store.statistics.field.FieldStatistic;

public class RasterInternalStatisticsHelper {

  public static RasterBoundingBoxValue getBbox(
      final DataStatisticsStore statisticsStore,
      final String typeName,
      final String... authorizations) {
    Statistic<RasterBoundingBoxValue> statistic =
        statisticsStore.getStatisticById(
            AdapterStatistic.generateStatisticId(
                typeName,
                RasterBoundingBoxStatistic.STATS_TYPE,
                Statistic.INTERNAL_TAG));
    if (statistic != null) {
      return statisticsStore.getStatisticValue(statistic, authorizations);
    }
    return null;
  }

}
