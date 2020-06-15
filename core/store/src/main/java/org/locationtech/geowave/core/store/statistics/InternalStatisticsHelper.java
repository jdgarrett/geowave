package org.locationtech.geowave.core.store.statistics;

import java.util.Collection;
import java.util.List;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.adapter.PersistentAdapterStore;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.adapter.DataTypeStatistic;
import org.locationtech.geowave.core.store.statistics.adapter.DataTypeStatisticType;
import org.locationtech.geowave.core.store.statistics.binning.CompositeBinningStrategy;
import org.locationtech.geowave.core.store.statistics.binning.DataTypeBinningStrategy;
import org.locationtech.geowave.core.store.statistics.binning.PartitionBinningStrategy;
import org.locationtech.geowave.core.store.statistics.field.FieldStatistic;
import org.locationtech.geowave.core.store.statistics.field.FieldStatisticType;
import org.locationtech.geowave.core.store.statistics.index.DifferingVisibilityCountStatistic;
import org.locationtech.geowave.core.store.statistics.index.DuplicateEntryCountStatistic;
import org.locationtech.geowave.core.store.statistics.index.FieldVisibilityCountStatistic;
import org.locationtech.geowave.core.store.statistics.index.IndexMetaDataSetStatistic;
import org.locationtech.geowave.core.store.statistics.index.IndexStatistic;
import org.locationtech.geowave.core.store.statistics.index.IndexStatisticType;
import org.locationtech.geowave.core.store.statistics.index.PartitionsStatistic;
import org.locationtech.geowave.core.store.statistics.index.RowRangeHistogramStatistic;
import org.locationtech.geowave.core.store.statistics.index.DifferingVisibilityCountStatistic.DifferingVisibilityCountValue;
import org.locationtech.geowave.core.store.statistics.index.PartitionsStatistic.PartitionsValue;
import org.locationtech.geowave.core.store.statistics.index.RowRangeHistogramStatistic.RowRangeHistogramValue;
import org.locationtech.geowave.core.store.statistics.index.DuplicateEntryCountStatistic.DuplicateEntryCountValue;
import org.locationtech.geowave.core.store.statistics.index.FieldVisibilityCountStatistic.FieldVisibilityCountValue;
import org.locationtech.geowave.core.store.statistics.index.IndexMetaDataSetStatistic.IndexMetaDataSetValue;

public class InternalStatisticsHelper {

  public static <V extends StatisticValue<R>, R> V getAdapterStatistic(
      final DataStatisticsStore statisticsStore,
      final DataTypeStatisticType<V> statisticType,
      final String typeName,
      final String... authorizations) {
    Statistic<V> statistic =
        statisticsStore.getStatisticById(
            DataTypeStatistic.generateStatisticId(typeName, statisticType, Statistic.INTERNAL_TAG));
    if (statistic != null) {
      return statisticsStore.getStatisticValue(statistic, authorizations);
    }
    return null;
  }

  public static <V extends StatisticValue<R>, R> V getFieldStatistic(
      final DataStatisticsStore statisticsStore,
      final FieldStatisticType<V> statisticType,
      final String typeName,
      final String fieldName,
      final String... authorizations) {
    Statistic<V> statistic =
        statisticsStore.getStatisticById(
            FieldStatistic.generateStatisticId(
                typeName,
                statisticType,
                fieldName,
                Statistic.INTERNAL_TAG));
    if (statistic != null) {
      return statisticsStore.getStatisticValue(statistic, authorizations);
    }
    return null;
  }

  public static DuplicateEntryCountValue getDuplicateCounts(
      final Index index,
      final Collection<Short> adapterIdsToQuery,
      final PersistentAdapterStore adapterStore,
      final DataStatisticsStore statisticsStore,
      final String... authorizations) {
    return getInternalIndexStatistic(
        DuplicateEntryCountStatistic.STATS_TYPE,
        index,
        adapterIdsToQuery,
        adapterStore,
        statisticsStore,
        authorizations);
  }

  public static IndexMetaDataSetValue getIndexMetadata(
      final Index index,
      final Collection<Short> adapterIdsToQuery,
      final PersistentAdapterStore adapterStore,
      final DataStatisticsStore statisticsStore,
      final String... authorizations) {
    return getInternalIndexStatistic(
        IndexMetaDataSetStatistic.STATS_TYPE,
        index,
        adapterIdsToQuery,
        adapterStore,
        statisticsStore,
        authorizations);
  }

  public static PartitionsValue getPartitions(
      final Index index,
      final Collection<Short> adapterIdsToQuery,
      final PersistentAdapterStore adapterStore,
      final DataStatisticsStore statisticsStore,
      final String... authorizations) {
    return getInternalIndexStatistic(
        PartitionsStatistic.STATS_TYPE,
        index,
        adapterIdsToQuery,
        adapterStore,
        statisticsStore,
        authorizations);
  }

  public static DifferingVisibilityCountValue getDifferingVisibilityCounts(
      final Index index,
      final Collection<Short> adapterIdsToQuery,
      final PersistentAdapterStore adapterStore,
      final DataStatisticsStore statisticsStore,
      final String... authorizations) {
    return getInternalIndexStatistic(
        DifferingVisibilityCountStatistic.STATS_TYPE,
        index,
        adapterIdsToQuery,
        adapterStore,
        statisticsStore,
        authorizations);
  }

  public static FieldVisibilityCountValue getVisibilityCounts(
      final Index index,
      final Collection<Short> adapterIdsToQuery,
      final PersistentAdapterStore adapterStore,
      final DataStatisticsStore statisticsStore,
      final String... authorizations) {
    return getInternalIndexStatistic(
        FieldVisibilityCountStatistic.STATS_TYPE,
        index,
        adapterIdsToQuery,
        adapterStore,
        statisticsStore,
        authorizations);
  }

  public static RowRangeHistogramValue getRangeStats(
      final Index index,
      final List<Short> adapterIds,
      final PersistentAdapterStore adapterStore,
      final DataStatisticsStore statisticsStore,
      final ByteArray partitionKey,
      final String... authorizations) {
    RowRangeHistogramStatistic stat =
        (RowRangeHistogramStatistic) statisticsStore.getStatisticById(
            IndexStatistic.generateStatisticId(
                index.getName(),
                RowRangeHistogramStatistic.STATS_TYPE,
                Statistic.INTERNAL_TAG));
    if (stat != null
        && stat.getBinningStrategy() instanceof CompositeBinningStrategy
        && ((CompositeBinningStrategy) stat.getBinningStrategy()).isOfType(
            DataTypeBinningStrategy.class,
            PartitionBinningStrategy.class)) {
      RowRangeHistogramValue combinedValue = null;
      for (Short adapterId : adapterIds) {
        RowRangeHistogramValue value =
            statisticsStore.getStatisticValue(
                stat,
                CompositeBinningStrategy.getBin(
                    DataTypeBinningStrategy.getBin(adapterStore.getAdapter(adapterId)),
                    PartitionBinningStrategy.getBin(partitionKey.getBytes())),
                authorizations);
        if (value != null) {
          if (combinedValue == null) {
            combinedValue = value;
          } else {
            combinedValue.merge(value);
          }
        }
      }
      return combinedValue;
    }
    return null;
  }

  public static RowRangeHistogramValue getRangeStats(
      final DataStatisticsStore statisticsStore,
      final String indexName,
      final String typeName,
      final ByteArray partitionKey,
      String... authorizations) {
    Statistic<RowRangeHistogramValue> statistic =
        statisticsStore.getStatisticById(
            IndexStatistic.generateStatisticId(
                indexName,
                RowRangeHistogramStatistic.STATS_TYPE,
                Statistic.INTERNAL_TAG));
    if (statistic != null
        && statistic.getBinningStrategy() instanceof CompositeBinningStrategy
        && ((CompositeBinningStrategy) statistic.getBinningStrategy()).isOfType(
            DataTypeBinningStrategy.class,
            PartitionBinningStrategy.class)) {
      return statisticsStore.getStatisticValue(
          statistic,
          CompositeBinningStrategy.getBin(
              DataTypeBinningStrategy.getBin(typeName),
              PartitionBinningStrategy.getBin(partitionKey.getBytes())),
          authorizations);
    }
    return null;
  }

  private static <V extends StatisticValue<R>, R> V getInternalIndexStatistic(
      final IndexStatisticType<V> statisticType,
      final Index index,
      final Collection<Short> adapterIdsToQuery,
      final PersistentAdapterStore adapterStore,
      final DataStatisticsStore statisticsStore,
      final String... authorizations) {
    StatisticId<V> statisticId =
        IndexStatistic.generateStatisticId(index.getName(), statisticType, Statistic.INTERNAL_TAG);
    Statistic<V> stat = statisticsStore.getStatisticById(statisticId);
    if (stat != null) {
      V combinedValue = null;
      for (final short adapterId : adapterIdsToQuery) {
        DataTypeAdapter<?> adapter = adapterStore.getAdapter(adapterId);

        V value =
            statisticsStore.getStatisticValue(
                stat,
                DataTypeBinningStrategy.getBin(adapter),
                authorizations);
        if (combinedValue == null) {
          combinedValue = value;
        } else {
          combinedValue.merge(value);
        }
      }
      return combinedValue;
    }
    return null;
  }

}
