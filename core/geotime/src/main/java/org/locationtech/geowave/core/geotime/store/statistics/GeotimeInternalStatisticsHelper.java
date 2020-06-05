package org.locationtech.geowave.core.geotime.store.statistics;

import org.locationtech.geowave.core.geotime.store.statistics.BoundingBoxStatistic.BoundingBoxValue;
import org.locationtech.geowave.core.geotime.store.statistics.TimeRangeStatistic.TimeRangeValue;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.statistics.DataStatisticsStore;
import org.locationtech.geowave.core.store.statistics.field.FieldStatistic;

public class GeotimeInternalStatisticsHelper {
  public static BoundingBoxValue getBbox(
      final DataStatisticsStore statisticsStore,
      final String typeName,
      final String fieldName,
      final String... authorizations) {
    Statistic<BoundingBoxValue> statistic =
        statisticsStore.getStatisticById(
            FieldStatistic.generateStatisticId(
                typeName,
                BoundingBoxStatistic.STATS_TYPE,
                fieldName,
                Statistic.INTERNAL_TAG));
    if (statistic != null) {
      return statisticsStore.getStatisticValue(statistic, authorizations);
    }
    return null;
  }

  public static TimeRangeValue getTimeRange(
      final DataStatisticsStore statisticsStore,
      final String typeName,
      final String fieldName,
      final String... authorizations) {
    Statistic<TimeRangeValue> statistic =
        statisticsStore.getStatisticById(
            FieldStatistic.generateStatisticId(
                typeName,
                TimeRangeStatistic.STATS_TYPE,
                fieldName,
                Statistic.INTERNAL_TAG));
    if (statistic != null) {
      return statisticsStore.getStatisticValue(statistic, authorizations);
    }
    return null;
  }
}
