package org.locationtech.geowave.core.store.statistics;

import java.util.Collection;
import java.util.List;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.adapter.PersistentAdapterStore;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.data.visibility.DifferingFieldVisibilityEntryCount;
import org.locationtech.geowave.core.store.data.visibility.FieldVisibilityCount;
import org.locationtech.geowave.core.store.data.visibility.DifferingFieldVisibilityEntryCount.DifferingFieldVisibilityEntryCountValue;
import org.locationtech.geowave.core.store.data.visibility.FieldVisibilityCount.FieldVisibilityCountValue;
import org.locationtech.geowave.core.store.index.IndexMetaDataSet;
import org.locationtech.geowave.core.store.index.IndexMetaDataSet.IndexMetaDataSetValue;
import org.locationtech.geowave.core.store.statistics.adapter.AdapterStatistic;
import org.locationtech.geowave.core.store.statistics.adapter.CountStatistic;
import org.locationtech.geowave.core.store.statistics.adapter.CountStatistic.CountValue;
import org.locationtech.geowave.core.store.statistics.index.DuplicateEntryCountStatistic;
import org.locationtech.geowave.core.store.statistics.index.IndexStatistic;
import org.locationtech.geowave.core.store.statistics.index.IndexStatisticType;
import org.locationtech.geowave.core.store.statistics.index.PartitionsStatistic;
import org.locationtech.geowave.core.store.statistics.index.RowRangeHistogramStatistic;
import org.locationtech.geowave.core.store.statistics.index.PartitionsStatistic.PartitionsValue;
import org.locationtech.geowave.core.store.statistics.index.RowRangeHistogramStatistic.RowRangeHistogramValue;
import org.locationtech.geowave.core.store.statistics.index.DuplicateEntryCountStatistic.DuplicateEntryCountValue;

public class InternalStatisticsHelper {

  public static CountValue getCount(
      final DataStatisticsStore statisticsStore,
      final String typeName,
      final String... authorizations) {
    Statistic<CountValue> statistic =
        statisticsStore.getStatisticById(
            AdapterStatistic.generateStatisticId(
                typeName,
                CountStatistic.STATS_TYPE,
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
        IndexMetaDataSet.STATS_TYPE,
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

  public static DifferingFieldVisibilityEntryCountValue getDifferingVisibilityCounts(
      final Index index,
      final Collection<Short> adapterIdsToQuery,
      final PersistentAdapterStore adapterStore,
      final DataStatisticsStore statisticsStore,
      final String... authorizations) {
    return getInternalIndexStatistic(
        DifferingFieldVisibilityEntryCount.STATS_TYPE,
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
        FieldVisibilityCount.STATS_TYPE,
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
            AdapterBinningStrategy.class,
            PartitionBinningStrategy.class)) {
      RowRangeHistogramValue combinedValue = null;
      for (Short adapterId : adapterIds) {
        RowRangeHistogramValue value =
            statisticsStore.getStatisticValue(
                stat,
                CompositeBinningStrategy.getBin(
                    AdapterBinningStrategy.getBin(adapterStore.getAdapter(adapterId)),
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
            AdapterBinningStrategy.class,
            PartitionBinningStrategy.class)) {
      return statisticsStore.getStatisticValue(
          statistic,
          CompositeBinningStrategy.getBin(
              AdapterBinningStrategy.getBin(typeName),
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
                AdapterBinningStrategy.getBin(adapter),
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
