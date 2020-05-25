package org.locationtech.geowave.core.store.metadata;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.ByteArrayUtils;
import org.locationtech.geowave.core.index.StringUtils;
import org.locationtech.geowave.core.store.CloseableIterator;
import org.locationtech.geowave.core.store.DataStoreOptions;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.operations.DataStoreOperations;
import org.locationtech.geowave.core.store.operations.MetadataQuery;
import org.locationtech.geowave.core.store.operations.MetadataType;
import org.locationtech.geowave.core.store.statistics.DataStatisticsStore;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.StatisticsRegistry;
import org.locationtech.geowave.core.store.statistics.StatisticsProviderSPI.ProvidedStatistic;
import org.locationtech.geowave.core.store.statistics.StatisticsProviderSPI.StatisticContext;
import org.locationtech.geowave.core.store.statistics.adapter.AdapterStatistic;
import org.locationtech.geowave.core.store.statistics.field.FieldStatistic;
import org.locationtech.geowave.core.store.statistics.index.IndexStatistic;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;

public class DataStatisticsStoreImpl extends AbstractGeoWavePersistence<Statistic<?>> implements
    DataStatisticsStore {
  public DataStatisticsStoreImpl(DataStoreOperations operations, DataStoreOptions options) {
    super(operations, options, MetadataType.STAT_OPTIONS);
  }

  @Override
  protected ByteArray getPrimaryId(Statistic<?> persistedObject) {
    return getPrimaryId(persistedObject.getStatisticType(), persistedObject.getUniqueId());
  }

  public static ByteArray getPrimaryId(final StatisticType type, final String extendedId) {
    if ((extendedId != null) && (extendedId.length() > 0)) {
      return new ByteArray(
          Bytes.concat(
              type.getBytes(),
              new byte[] {(byte) Statistic.UNIQUE_ID_SEPARATOR},
              StringUtils.stringToBinary(extendedId)));
    }
    return type;
  }

  @Override
  protected ByteArray getSecondaryId(Statistic<?> persistedObject) {
    if (persistedObject instanceof IndexStatistic) {
      return indexStatisticSecondaryId(((IndexStatistic<?>) persistedObject).getIndexName());
    } else if (persistedObject instanceof AdapterStatistic) {
      return new ByteArray(
          ("A" + ((AdapterStatistic<?>) persistedObject).getTypeName()).getBytes());
    } else if (persistedObject instanceof FieldStatistic) {
      return new ByteArray(("F" + ((FieldStatistic<?>) persistedObject).getTypeName()).getBytes());
    }
    return null;
  }

  private ByteArray indexStatisticSecondaryId(final String indexName) {
    return new ByteArray(("I" + indexName).getBytes());
  }

  private ByteArray adapterStatisticSecondaryId(final String typeName) {
    return new ByteArray(("A" + typeName).getBytes());
  }

  private ByteArray fieldStatisticSecondaryId(final String typeName) {
    return new ByteArray(("F" + typeName).getBytes());
  }

  @Override
  public List<Statistic<?>> getRegisteredIndexStatistics() {
    return StatisticsRegistry.instance().getStatistics().values().stream().filter(
        ProvidedStatistic::isIndexStatistic).map(s -> s.getOptionsConstructor().get()).collect(
            Collectors.toList());
  }

  @Override
  public List<Statistic<?>> getRegisteredAdapterStatistics(Class<?> adapterDataClass) {
    return StatisticsRegistry.instance().getStatistics().values().stream().filter(
        s -> s.isAdapterStatistic() && s.isCompatibleWith(adapterDataClass)).map(
            s -> s.getOptionsConstructor().get()).collect(Collectors.toList());
  }

  @Override
  public Map<String, List<Statistic<?>>> getRegisteredFieldStatistics(
      DataTypeAdapter<?> type,
      String fieldName) {
    Map<String, List<Statistic<?>>> statistics = Maps.newHashMap();
    final int fieldCount = type.getFieldCount();
    for (int i = 0; i < fieldCount; i++) {
      String name = type.getFieldName(i);
      Class<?> fieldClass = type.getFieldClass(i);
      if (fieldName == null || fieldName.equals(name)) {
        List<Statistic<?>> fieldOptions =
            StatisticsRegistry.instance().getStatistics().values().stream().filter(
                s -> s.isFieldStatistic() && s.isCompatibleWith(fieldClass)).map(
                    s -> s.getOptionsConstructor().get()).collect(Collectors.toList());
        statistics.put(name, fieldOptions);
      }
    }
    return statistics;
  }

  @Override
  public boolean exists(Statistic<?> statistic) {
    return getObject(getPrimaryId(statistic), getSecondaryId(statistic)) != null;
  }

  @Override
  public void addStatistic(Statistic<?> statistic) {
    this.addObject(statistic);
  }

  @Override
  public boolean removeStatistic(Statistic<?> statistic) {
    return deleteObject(getPrimaryId(statistic), getSecondaryId(statistic));
  }

  @Override
  public boolean removeStatistics(Iterator<Statistic<?>> statistics) {
    boolean deleted = false;
    while (statistics.hasNext()) {
      Statistic<?> statistic = statistics.next();
      deleted = deleted || deleteObject(getPrimaryId(statistic), getSecondaryId(statistic));
    }
    return deleted;
  }

  @Override
  public boolean removeStatistics(final Index index) {
    return removeStatistics(getIndexStatistics(index, null, null));
  }

  @Override
  public boolean removeStatistics(final DataTypeAdapter<?> type) {
    boolean removed = removeStatistics(getAdapterStatistics(type, null));
    // STATS_TODO: Remove all index statistics with this type name
    return removed;
  }

  @Override
  public void setStatistic(Statistic<?> statistic) {
    // TODO Auto-generated method stub

  }

  @Override
  public void incorporateStatistic(Statistic<?> statistic) {
    // TODO Auto-generated method stub

  }

  protected CloseableIterator<Statistic<?>> getCachedObject(
      ByteArray primaryId,
      ByteArray secondaryId) {
    final Object cacheResult = getObjectFromCache(primaryId, secondaryId);

    // if there's an exact match in the cache return a singleton
    if (cacheResult != null) {
      return new CloseableIterator.Wrapper<>(
          Iterators.singletonIterator((Statistic<?>) cacheResult));
    }
    return internalGetObjects(new MetadataQuery(primaryId.getBytes(), secondaryId.getBytes()));
  }

  @Override
  public CloseableIterator<Statistic<?>> getIndexStatistics(
      final Index index,
      final @Nullable StatisticType statisticType,
      final @Nullable String typeName) {
    final ByteArray secondaryId = indexStatisticSecondaryId(index.getName());
    if (statisticType == null) {
      CloseableIterator<Statistic<?>> stats = getAllObjectsWithSecondaryId(secondaryId);
      if (typeName == null) {
        return stats;
      }
      return new SuffixPrimaryIdFilter(stats, typeName.getBytes());
    } else if (typeName == null) {
      return internalGetObjects(
          new MetadataQuery(statisticType.getBytes(), secondaryId.getBytes()));
    }
    return getCachedObject(getPrimaryId(statisticType, typeName), secondaryId);

  }

  @Override
  public CloseableIterator<Statistic<?>> getAdapterStatistics(
      final DataTypeAdapter<?> type,
      final @Nullable StatisticType statisticType) {
    final ByteArray secondaryId = adapterStatisticSecondaryId(type.getTypeName());
    if (statisticType == null) {
      return getAllObjectsWithSecondaryId(secondaryId);
    }
    return getCachedObject(statisticType, secondaryId);
  }

  @Override
  public CloseableIterator<Statistic<?>> getFieldStatistics(
      final DataTypeAdapter<?> type,
      final @Nullable StatisticType statisticType,
      final @Nullable String fieldName) {
    final ByteArray secondaryId = fieldStatisticSecondaryId(type.getTypeName());
    if (statisticType == null) {
      CloseableIterator<Statistic<?>> stats = getAllObjectsWithSecondaryId(secondaryId);
      if (fieldName == null) {
        return stats;
      }
      return new SuffixPrimaryIdFilter(stats, fieldName.getBytes());
    } else if (fieldName == null) {
      return internalGetObjects(
          new MetadataQuery(statisticType.getBytes(), secondaryId.getBytes()));
    }
    return getCachedObject(getPrimaryId(statisticType, fieldName), secondaryId);
  }

  @Override
  public CloseableIterator<Statistic<?>> getAllStatistics(
      final @Nullable StatisticType statisticType) {
    return internalGetObjects(
        new MetadataQuery(statisticType == null ? null : statisticType.getBytes(), null));
  }

  @Override
  public CloseableIterator<StatisticValue<?>> getStatisticValues(
      Iterator<Statistic<?>> statistics,
      String... authorizations) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> StatisticValue<T> getStatisticValue(Statistic<T> statistic, String... authorizations) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeAll() {
    super.removeAll();
  }

  // STATS_TODO: This needs to be a little more complex, for example, a statistic might have a
  // primary id of STAT_TYPE_fieldName_extended. Find a way to isolate the part of the primaryId
  // that we want.. Maybe some kind of separator for the extended part...
  public static class SuffixPrimaryIdFilter implements CloseableIterator<Statistic<?>> {

    private final CloseableIterator<Statistic<?>> source;
    private final byte[] suffix;

    private Statistic<?> next = null;

    public SuffixPrimaryIdFilter(CloseableIterator<Statistic<?>> source, byte[] suffix) {
      this.source = source;
      this.suffix = suffix;
    }

    private void computeNext() {
      while (source.hasNext()) {
        Statistic<?> possibleNext = source.next();
        ByteArray primaryId =
            getPrimaryId(possibleNext.getStatisticType(), possibleNext.getUniqueId());
        if (ByteArrayUtils.endsWith(primaryId.getBytes(), suffix)) {
          next = possibleNext;
          break;
        }
      }
    }

    @Override
    public boolean hasNext() {
      if (next == null) {
        computeNext();
      }
      return next != null;
    }

    @Override
    public Statistic<?> next() {
      if (next == null) {
        computeNext();
      }
      Statistic<?> nextValue = next;
      next = null;
      return nextValue;
    }

    @Override
    public void close() {
      source.close();
    }

  }
}
