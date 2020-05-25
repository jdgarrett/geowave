package org.locationtech.geowave.core.store.statistics;

import org.locationtech.geowave.core.store.statistics.adapter.CountStatistic;
import org.locationtech.geowave.core.store.statistics.field.FixedBinNumericHistogramStatistic;
import org.locationtech.geowave.core.store.statistics.field.NumericRangeStatistic;
import org.locationtech.geowave.core.store.statistics.index.DuplicateEntryCountStatistic;

public class DefaultStatisticsProvider implements StatisticsProviderSPI {
  public ProvidedStatistic[] getProvidedStatistics() {
    return new ProvidedStatistic[] {
        // Index Statistics
        new ProvidedStatistic(
            DuplicateEntryCountStatistic.STATS_TYPE,
            DuplicateEntryCountStatistic::new),
        // Adapter Statistics
        new ProvidedStatistic(CountStatistic.STATS_TYPE, CountStatistic::new),
        // Field Statistics
        new ProvidedStatistic(
            FixedBinNumericHistogramStatistic.STATS_TYPE,
            FixedBinNumericHistogramStatistic::new),
        new ProvidedStatistic(NumericRangeStatistic.STATS_TYPE, NumericRangeStatistic::new)};
  }
}
