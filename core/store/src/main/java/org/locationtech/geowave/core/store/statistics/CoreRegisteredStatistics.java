package org.locationtech.geowave.core.store.statistics;

import org.locationtech.geowave.core.store.statistics.adapter.CountStatistic;
import org.locationtech.geowave.core.store.statistics.field.FixedBinNumericHistogramStatistic;
import org.locationtech.geowave.core.store.statistics.field.NumericRangeStatistic;
import org.locationtech.geowave.core.store.statistics.index.DuplicateEntryCountStatistic;
import org.locationtech.geowave.core.store.statistics.index.MaxDuplicatesStatistic;
import org.locationtech.geowave.core.store.statistics.index.PartitionsStatistic;
import org.locationtech.geowave.core.store.statistics.index.RowRangeHistogramStatistic;

public class CoreRegisteredStatistics implements StatisticsRegistrySPI {
  public RegisteredStatistic[] getProvidedStatistics() {
    return new RegisteredStatistic[] {
        // Index Statistics
        new RegisteredStatistic(
            DuplicateEntryCountStatistic.STATS_TYPE,
            DuplicateEntryCountStatistic::new),
        new RegisteredStatistic(MaxDuplicatesStatistic.STATS_TYPE, MaxDuplicatesStatistic::new),
        new RegisteredStatistic(PartitionsStatistic.STATS_TYPE, PartitionsStatistic::new),
        new RegisteredStatistic(
            RowRangeHistogramStatistic.STATS_TYPE,
            RowRangeHistogramStatistic::new),

        // Adapter Statistics
        new RegisteredStatistic(CountStatistic.STATS_TYPE, CountStatistic::new),

        // Field Statistics
        new RegisteredStatistic(
            FixedBinNumericHistogramStatistic.STATS_TYPE,
            FixedBinNumericHistogramStatistic::new),
        new RegisteredStatistic(NumericRangeStatistic.STATS_TYPE, NumericRangeStatistic::new)};
  }

  @Override
  public RegisteredBinningStrategy[] getProvidedBinningStrategies() {
    return new RegisteredBinningStrategy[] {
        new RegisteredBinningStrategy("PARTITION", PartitionBinningStrategy::new)};
  }
}
