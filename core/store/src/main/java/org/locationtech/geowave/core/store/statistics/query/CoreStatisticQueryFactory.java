package org.locationtech.geowave.core.store.statistics.query;

import java.util.Set;
import org.apache.commons.lang3.Range;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.adapter.statistics.histogram.FixedBinNumericHistogram;
import org.locationtech.geowave.core.store.adapter.statistics.histogram.NumericHistogram;
import org.locationtech.geowave.core.store.statistics.adapter.CountStatistic;
import org.locationtech.geowave.core.store.statistics.adapter.CountStatistic.CountValue;
import org.locationtech.geowave.core.store.statistics.field.FixedBinNumericHistogramStatistic;
import org.locationtech.geowave.core.store.statistics.field.NumericRangeStatistic;
import org.locationtech.geowave.core.store.statistics.field.NumericRangeStatistic.NumericRangeValue;
import org.locationtech.geowave.core.store.statistics.field.FixedBinNumericHistogramStatistic.FixedBinNumericHistogramValue;
import org.locationtech.geowave.core.store.statistics.index.DuplicateEntryCountStatistic;
import org.locationtech.geowave.core.store.statistics.index.PartitionsStatistic;
import org.locationtech.geowave.core.store.statistics.index.PartitionsStatistic.PartitionsValue;
import org.locationtech.geowave.core.store.statistics.index.DuplicateEntryCountStatistic.DuplicateEntryCountValue;
import org.locationtech.geowave.core.store.statistics.index.RowRangeHistogramStatistic;
import org.locationtech.geowave.core.store.statistics.index.RowRangeHistogramStatistic.RowRangeHistogramValue;

public class CoreStatisticQueryFactory {
  // Index Statistics
  public static IndexStatisticQueryBuilder<RowRangeHistogramValue, NumericHistogram> rowRanges() {
    return new IndexStatisticQueryBuilder<>(RowRangeHistogramStatistic.STATS_TYPE);
  }

  public static IndexStatisticQueryBuilder<DuplicateEntryCountValue, Long> duplicateCounts() {
    return new IndexStatisticQueryBuilder<>(DuplicateEntryCountStatistic.STATS_TYPE);
  }

  public static IndexStatisticQueryBuilder<PartitionsValue, Set<ByteArray>> partitions() {
    return new IndexStatisticQueryBuilder<>(PartitionsStatistic.STATS_TYPE);
  }

  // Adapter Statistics
  public static AdapterStatisticQueryBuilder<CountValue, Long> count() {
    return new AdapterStatisticQueryBuilder<>(CountStatistic.STATS_TYPE);
  }

  // Field Statistics
  public static FieldStatisticQueryBuilder<FixedBinNumericHistogramValue, FixedBinNumericHistogram> fixedBinHistogram() {
    return new FieldStatisticQueryBuilder<>(FixedBinNumericHistogramStatistic.STATS_TYPE);
  }

  public static FieldStatisticQueryBuilder<NumericRangeValue, Range<Double>> numericRange() {
    return new FieldStatisticQueryBuilder<>(NumericRangeStatistic.STATS_TYPE);
  }
}
