package org.locationtech.geowave.core.store.statistics;

import org.locationtech.geowave.core.store.statistics.adapter.CountStatistic;
import org.locationtech.geowave.core.store.statistics.adapter.CountStatistic.CountValue;
import org.locationtech.geowave.core.store.statistics.field.FixedBinNumericHistogramStatistic;
import org.locationtech.geowave.core.store.statistics.field.FixedBinNumericHistogramStatistic.FixedBinNumericHistogramValue;
import org.locationtech.geowave.core.store.statistics.field.NumericRangeStatistic;
import org.locationtech.geowave.core.store.statistics.field.NumericRangeStatistic.NumericRangeValue;
import org.locationtech.geowave.core.store.statistics.index.DifferingVisibilityCountStatistic;
import org.locationtech.geowave.core.store.statistics.index.DifferingVisibilityCountStatistic.DifferingVisibilityCountValue;
import org.locationtech.geowave.core.store.statistics.index.DuplicateEntryCountStatistic;
import org.locationtech.geowave.core.store.statistics.index.FieldVisibilityCountStatistic;
import org.locationtech.geowave.core.store.statistics.index.IndexMetaDataSetStatistic;
import org.locationtech.geowave.core.store.statistics.index.IndexMetaDataSetStatistic.IndexMetaDataSetValue;
import org.locationtech.geowave.core.store.statistics.index.FieldVisibilityCountStatistic.FieldVisibilityCountValue;
import org.locationtech.geowave.core.store.statistics.index.DuplicateEntryCountStatistic.DuplicateEntryCountValue;
import org.locationtech.geowave.core.store.statistics.index.MaxDuplicatesStatistic;
import org.locationtech.geowave.core.store.statistics.index.MaxDuplicatesStatistic.MaxDuplicatesValue;
import org.locationtech.geowave.core.store.statistics.index.PartitionsStatistic;
import org.locationtech.geowave.core.store.statistics.index.PartitionsStatistic.PartitionsValue;
import org.locationtech.geowave.core.store.statistics.index.RowRangeHistogramStatistic;
import org.locationtech.geowave.core.store.statistics.index.RowRangeHistogramStatistic.RowRangeHistogramValue;

public class CoreRegisteredStatistics extends StatisticsRegistrySPI {

  @Override
  public RegisteredStatistic[] getProvidedStatistics() {
    return new RegisteredStatistic[] {
        // Index Statistics
        new RegisteredStatistic(
            DifferingVisibilityCountStatistic.STATS_TYPE,
            DifferingVisibilityCountStatistic::new,
            DifferingVisibilityCountValue::new,
            (short) 2000,
            (short) 2001),
        new RegisteredStatistic(
            DuplicateEntryCountStatistic.STATS_TYPE,
            DuplicateEntryCountStatistic::new,
            DuplicateEntryCountValue::new,
            (short) 2002,
            (short) 2003),
        new RegisteredStatistic(
            FieldVisibilityCountStatistic.STATS_TYPE,
            FieldVisibilityCountStatistic::new,
            FieldVisibilityCountValue::new,
            (short) 2004,
            (short) 2005),
        new RegisteredStatistic(
            IndexMetaDataSetStatistic.STATS_TYPE,
            IndexMetaDataSetStatistic::new,
            IndexMetaDataSetValue::new,
            (short) 2006,
            (short) 2007),
        new RegisteredStatistic(
            MaxDuplicatesStatistic.STATS_TYPE,
            MaxDuplicatesStatistic::new,
            MaxDuplicatesValue::new,
            (short) 2008,
            (short) 2009),
        new RegisteredStatistic(
            PartitionsStatistic.STATS_TYPE,
            PartitionsStatistic::new,
            PartitionsValue::new,
            (short) 2010,
            (short) 2011),
        new RegisteredStatistic(
            RowRangeHistogramStatistic.STATS_TYPE,
            RowRangeHistogramStatistic::new,
            RowRangeHistogramValue::new,
            (short) 2012,
            (short) 2013),

        // Adapter Statistics
        new RegisteredStatistic(
            CountStatistic.STATS_TYPE,
            CountStatistic::new,
            CountValue::new,
            (short) 2014,
            (short) 2015),

        // Field Statistics
        new RegisteredStatistic(
            FixedBinNumericHistogramStatistic.STATS_TYPE,
            FixedBinNumericHistogramStatistic::new,
            FixedBinNumericHistogramValue::new,
            (short) 2016,
            (short) 2017),
        new RegisteredStatistic(
            NumericRangeStatistic.STATS_TYPE,
            NumericRangeStatistic::new,
            NumericRangeValue::new,
            (short) 2018,
            (short) 2019)};
  }

  @Override
  public RegisteredBinningStrategy[] getProvidedBinningStrategies() {
    return new RegisteredBinningStrategy[] {
        new RegisteredBinningStrategy("PARTITION", PartitionBinningStrategy::new, (short) 2020),
        new RegisteredBinningStrategy("ADAPTER", AdapterBinningStrategy::new, (short) 2021),
        new RegisteredBinningStrategy("COMPOSITE", CompositeBinningStrategy::new, (short) 2022)};
  }
}
