package org.locationtech.geowave.core.store.statistics;

import java.util.Collection;
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
import org.locationtech.geowave.core.store.statistics.adapter.CountStatistic;
import org.locationtech.geowave.core.store.statistics.index.DuplicateEntryCountStatistic;
import org.locationtech.geowave.core.store.statistics.index.IndexStatistic;
import org.locationtech.geowave.core.store.statistics.index.DuplicateEntryCountStatistic.DuplicateEntryCountValue;

public class InternalStatisticsHelper {
  
  public static final String GEOWAVE_INTERNAL_STATISTIC_NAME = "GEOWAVE";
  
  public static void initializeInternalIndexStatistics(final DataStatisticsStore statisticsStore, final Index index) {
    DuplicateEntryCountStatistic duplicateCounts = new DuplicateEntryCountStatistic();
    duplicateCounts.setName(GEOWAVE_INTERNAL_STATISTIC_NAME);
    duplicateCounts.setIndexName(index.getName());
    duplicateCounts.setBinningStrategy(new AdapterBinningStrategy());
    statisticsStore.addStatistic(duplicateCounts);


  }

  public static void initializeInternalAdapterStatistics(
      final DataStatisticsStore statisticsStore,
      final DataTypeAdapter<?> adapter) {
    // STATS_TODO: Move these to some kind of DefaultStatistics interface so they can be defined by
    // the adapter/index..
    CountStatistic count = new CountStatistic();
    count.setTypeName(adapter.getTypeName());
    count.setName(GEOWAVE_INTERNAL_STATISTIC_NAME);
    statisticsStore.addStatistic(count);
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

  private static <V extends StatisticValue<R>, R> V getInternalIndexStatistic(
      final StatisticType<V> statisticType,
      final Index index,
      final Collection<Short> adapterIdsToQuery,
      final PersistentAdapterStore adapterStore,
      final DataStatisticsStore statisticsStore,
      final String... authorizations) {
    StatisticId<V> statisticId =
        IndexStatistic.generateStatisticId(
            index.getName(),
            statisticType,
            GEOWAVE_INTERNAL_STATISTIC_NAME);
    Statistic<V> stat = statisticsStore.getStatisticById(statisticId);
    if (stat != null) {
      V combinedMetaData = null;
      for (final short adapterId : adapterIdsToQuery) {
        DataTypeAdapter<?> adapter = adapterStore.getAdapter(adapterId);

        V value =
            statisticsStore.getStatisticValue(
                stat,
                new ByteArray(adapter.getTypeName()),
                authorizations);
        if (combinedMetaData == null) {
          combinedMetaData = value;
        } else {
          combinedMetaData.merge(value);
        }
      }
      return combinedMetaData;
    }
    return null;
  }

}
