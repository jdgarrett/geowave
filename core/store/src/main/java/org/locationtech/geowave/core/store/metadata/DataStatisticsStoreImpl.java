package org.locationtech.geowave.core.store.metadata;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.CloseableIterator;
import org.locationtech.geowave.core.store.DataStoreOptions;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.operations.DataStoreOperations;
import org.locationtech.geowave.core.store.operations.MetadataDeleter;
import org.locationtech.geowave.core.store.operations.MetadataQuery;
import org.locationtech.geowave.core.store.operations.MetadataType;
import org.locationtech.geowave.core.store.statistics.BinnedStatisticValue;
import org.locationtech.geowave.core.store.statistics.DataStatisticsStore;
import org.locationtech.geowave.core.store.statistics.FieldStatisticId;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.StatisticUpdateCallback;
import org.locationtech.geowave.core.store.statistics.StatisticValueReader;
import org.locationtech.geowave.core.store.statistics.StatisticValueWriter;
import org.locationtech.geowave.core.store.statistics.StatisticsRegistry;
import org.locationtech.geowave.core.store.statistics.StatisticsRegistrySPI.RegisteredStatistic;
import org.locationtech.geowave.core.store.statistics.adapter.AdapterStatistic;
import org.locationtech.geowave.core.store.statistics.field.FieldStatistic;
import org.locationtech.geowave.core.store.statistics.index.IndexStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

public class DataStatisticsStoreImpl extends AbstractGeoWavePersistence<Statistic<? extends StatisticValue<?>>> implements
    DataStatisticsStore {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataStatisticsStoreImpl.class);
  //this is fairly arbitrary at the moment because it is the only custom
  // server op added
  public static final int STATS_COMBINER_PRIORITY = 10;
  public static final String STATISTICS_COMBINER_NAME = "STATS_COMBINER";
 
  public DataStatisticsStoreImpl(DataStoreOperations operations, DataStoreOptions options) {
    super(operations, options, MetadataType.STATS);
  }

  @Override
  protected ByteArray getPrimaryId(Statistic<? extends StatisticValue<?>> persistedObject) {
    return persistedObject.getId().getUniqueId();
  }

  @Override
  protected ByteArray getSecondaryId(Statistic<? extends StatisticValue<?>> persistedObject) {
    if (persistedObject instanceof IndexStatistic) {
      return indexStatisticSecondaryId(((IndexStatistic<?>) persistedObject).getIndexName());
    } else if (persistedObject instanceof AdapterStatistic) {
      return adapterStatisticSecondaryId(((AdapterStatistic<?>) persistedObject).getTypeName());
    } else if (persistedObject instanceof FieldStatistic) {
      return fieldStatisticSecondaryId(((FieldStatistic<?>) persistedObject).getTypeName());
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
  public List<? extends Statistic<? extends StatisticValue<?>>> getRegisteredIndexStatistics() {
    return StatisticsRegistry.instance().getStatistics().values().stream().filter(
        RegisteredStatistic::isIndexStatistic).map(s -> s.getOptionsConstructor().get()).collect(
            Collectors.toList());
  }

  @Override
  public List<? extends Statistic<? extends StatisticValue<?>>> getRegisteredAdapterStatistics(Class<?> adapterDataClass) {
    return StatisticsRegistry.instance().getStatistics().values().stream().filter(
        s -> s.isAdapterStatistic() && s.isCompatibleWith(adapterDataClass)).map(
            s -> s.getOptionsConstructor().get()).collect(Collectors.toList());
  }

  @Override
  public Map<String, List<? extends Statistic<? extends StatisticValue<?>>>> getRegisteredFieldStatistics(
      DataTypeAdapter<?> type,
      String fieldName) {
    Map<String, List<? extends Statistic<? extends StatisticValue<?>>>> statistics = Maps.newHashMap();
    final int fieldCount = type.getFieldCount();
    for (int i = 0; i < fieldCount; i++) {
      String name = type.getFieldName(i);
      Class<?> fieldClass = type.getFieldClass(i);
      if (fieldName == null || fieldName.equals(name)) {
        List<Statistic<StatisticValue<Object>>> fieldOptions =
            StatisticsRegistry.instance().getStatistics().values().stream().filter(
                s -> s.isFieldStatistic() && s.isCompatibleWith(fieldClass)).map(
                    s -> s.getOptionsConstructor().get()).collect(Collectors.toList());
        statistics.put(name, fieldOptions);
      }
    }
    return statistics;
  }

  @Override
  public boolean exists(Statistic<? extends StatisticValue<?>> statistic) {
    return getObject(getPrimaryId(statistic), getSecondaryId(statistic)) != null;
  }

  @Override
  public void addStatistic(Statistic<? extends StatisticValue<?>> statistic) {
    // STATS_TODO: There needs to be a way to validate the statistic (maybe a statistic.validate() function that throws an exception)
    this.addObject(statistic);
  }

  @Override
  public boolean removeStatistic(Statistic<? extends StatisticValue<?>> statistic) {
    return deleteObject(getPrimaryId(statistic), getSecondaryId(statistic));
  }

  @Override
  public boolean removeStatistics(Iterator<? extends Statistic<? extends StatisticValue<?>>> statistics) {
    boolean deleted = false;
    while (statistics.hasNext()) {
      Statistic<? extends StatisticValue<?>> statistic = statistics.next();
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
    boolean removed = removeStatistics(getAdapterStatistics(type, null, null));
    // STATS_TODO: Remove all bins for this type for index statistics that are binned by adapter.
    return removed;
  }

  @SuppressWarnings("unchecked")
  protected CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getCachedObject(
      ByteArray primaryId,
      ByteArray secondaryId) {
    final Object cacheResult = getObjectFromCache(primaryId, secondaryId);

    // if there's an exact match in the cache return a singleton
    if (cacheResult != null) {
      return new CloseableIterator.Wrapper<>(
          Iterators.singletonIterator((Statistic<StatisticValue<Object>>) cacheResult));
    }
    return internalGetObjects(new MetadataQuery(primaryId.getBytes(), secondaryId.getBytes()));
  }
  
  protected CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getBasicStatisticsInternal(
      final ByteArray secondaryId,
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType,
      final @Nullable String name) {
    if (statisticType == null) {
      CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> stats = getAllObjectsWithSecondaryId(secondaryId);
      if (name == null) {
        return stats;
      }
      return new NameFilter(stats, name);
    } else if (name == null) {
      return internalGetObjects(
          new MetadataQuery(statisticType.getBytes(), secondaryId.getBytes()));
    }
    return getCachedObject(StatisticId.generateUniqueId(statisticType, name), secondaryId);
  }
  
  protected CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getFieldStatisticsInternal(
      final ByteArray secondaryId,
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType,
      final @Nullable String fieldName,
      final @Nullable String name) {
    if (statisticType != null) {
      if (fieldName != null) {
        ByteArray primaryId = FieldStatisticId.generateUniqueId(statisticType, fieldName, name);
        if (name != null) {
          return getCachedObject(primaryId, secondaryId);
        } else {
          return internalGetObjects(
              new MetadataQuery(primaryId.getBytes(), secondaryId.getBytes()));
        }
      } else {
        if (name != null) {
          return new NameFilter(internalGetObjects(
              new MetadataQuery(statisticType.getBytes(), secondaryId.getBytes())), name);
        } else {
          return internalGetObjects(
              new MetadataQuery(statisticType.getBytes(), secondaryId.getBytes()));
        }
      }
    }
    return new FieldStatisticFilter(getAllObjectsWithSecondaryId(secondaryId), fieldName, name);
  }
  
  protected CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getAllStatisticsInternal(
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType) {
    return internalGetObjects(
        new MetadataQuery(statisticType == null ? null : statisticType.getBytes(), null));
  }

  @Override
  public CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getIndexStatistics(
      final Index index,
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType,
      final @Nullable String name) {
    return getBasicStatisticsInternal(
        indexStatisticSecondaryId(index.getName()),
        statisticType,
        name);
  }

  @Override
  public CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getAdapterStatistics(
      final DataTypeAdapter<?> type,
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType,
      final @Nullable String name) {
    return getBasicStatisticsInternal(
        adapterStatisticSecondaryId(type.getTypeName()),
        statisticType,
        name);
  }

  @Override
  public CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getFieldStatistics(
      final DataTypeAdapter<?> type,
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType,
      final @Nullable String fieldName,
      final @Nullable String name) {
    return getFieldStatisticsInternal(
        fieldStatisticSecondaryId(type.getTypeName()),
        statisticType,
        fieldName,
        name);

  }
  
  @SuppressWarnings("unchecked")
  @Override
  public CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getAllStatistics(
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType) {
    return (CloseableIterator<Statistic<StatisticValue<Object>>>) getAllStatisticsInternal(statisticType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends StatisticValue<R>, R> Statistic<V> getStatisticById(StatisticId<V> statisticId) {
    try(CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> iterator = getCachedObject(statisticId.getUniqueId(), statisticId.getGroupId())) {
      if (iterator.hasNext()) {
        return (Statistic<V>) iterator.next();
      }
    }
    return null;
  }
  

  @Override
  public <V extends StatisticValue<R>, R> void setStatisticValue(
      Statistic<V> statistic,
      V value) {
    if (statistic.getBinningStrategy() != null) {
      throw new UnsupportedOperationException("The given statistic uses a binning strategy, but no bin was specified.");
    }
    removeStatisticValue(statistic);
    incorporateStatisticValue(statistic, value);
  }
  
  @Override
  public <V extends StatisticValue<R>, R> void setStatisticValue(
      Statistic<V> statistic,
      V value,
      ByteArray bin) {
    if (statistic.getBinningStrategy() == null) {
      throw new UnsupportedOperationException("The given statistic does not use a binning strategy, but a bin was specified.");
    }
    removeStatisticValue(statistic, bin);
    incorporateStatisticValue(statistic, value, bin);
  }

  @Override
  public <V extends StatisticValue<R>, R> void incorporateStatisticValue(
      Statistic<V> statistic,
      V value) {
    if (statistic.getBinningStrategy() != null) {
      throw new UnsupportedOperationException("The given statistic uses a binning strategy, but no bin was specified.");
    }
    try (StatisticValueWriter<V> writer = createStatisticValueWriter(statistic)) {
      writer.writeStatisticValue(null, null, value);
    } catch (Exception e) {
      LOGGER.error("Unable to write statistic value", e);
    }
  }
  
  @Override
  public <V extends StatisticValue<R>, R> void incorporateStatisticValue(
      Statistic<V> statistic,
      V value,
      ByteArray bin) {
    if (statistic.getBinningStrategy() == null) {
      throw new UnsupportedOperationException("The given statistic does not use a binning strategy, but a bin was specified.");
    }
    try (StatisticValueWriter<V> writer = createStatisticValueWriter(statistic)) {
      writer.writeStatisticValue(bin.getBytes(), null, value);
    } catch (Exception e) {
      LOGGER.error("Unable to write statistic value", e);
    }
  }
  

  @Override
  public <V extends StatisticValue<R>, R> StatisticValueWriter<V> createStatisticValueWriter(Statistic<V> statistic) {
    return new StatisticValueWriter<>(operations.createMetadataWriter(MetadataType.STAT_VALUES), statistic);
  }
  
  public <V extends StatisticValue<R>, R> StatisticValueReader<V, R> createStatisticValueReader(Statistic<V> statistic, ByteArray bin) {
    MetadataQuery query = new MetadataQuery(StatisticValue.getValueId(statistic.getId(), bin), statistic.getId().getGroupId().getBytes());
    return new StatisticValueReader<>(operations.createMetadataReader(MetadataType.STAT_VALUES).query(query), statistic);
  }

  @Override
  public <V extends StatisticValue<R>, R> boolean removeStatisticValue(Statistic<V> statistic) {
    if (statistic.getBinningStrategy() != null) {
      throw new UnsupportedOperationException("The given statistic uses a binning strategy, but no bin was specified.");
    }
    boolean deleted = false;
    try (MetadataDeleter deleter = operations.createMetadataDeleter(MetadataType.STAT_VALUES)) {
      deleted = deleter.delete(
          new MetadataQuery(
              StatisticValue.getValueId(statistic.getId(), null),
              statistic.getId().getGroupId().getBytes()));
    } catch (Exception e) {
      LOGGER.error("Unable to remove value for statistic", e);
    }
    return deleted;
  }
  
  @Override
  public <V extends StatisticValue<R>, R> boolean removeStatisticValue(Statistic<V> statistic, ByteArray bin) {
    if (statistic.getBinningStrategy() == null) {
      throw new UnsupportedOperationException("The given statistic does not use a binning strategy, but a bin was specified.");
    }
    boolean deleted = false;
    try (MetadataDeleter deleter = operations.createMetadataDeleter(MetadataType.STAT_VALUES)) {
      deleted = deleter.delete(
          new MetadataQuery(
              StatisticValue.getValueId(statistic.getId(), bin),
              statistic.getId().getGroupId().getBytes()));
    } catch (Exception e) {
      LOGGER.error("Unable to remove value for statistic", e);
    }
    return deleted;
  }
  
  @Override
  public <V extends StatisticValue<R>, R> boolean removeStatisticValues(Statistic<V> statistic) {
    boolean deleted = false;
    try (MetadataDeleter deleter = operations.createMetadataDeleter(MetadataType.STAT_VALUES)) {
      deleted = deleter.delete(
          new MetadataQuery(
              StatisticValue.getValueId(statistic.getId(), null),
              statistic.getId().getGroupId().getBytes()));
    } catch (Exception e) {
      LOGGER.error("Unable to remove values for statistic", e);
    }
    return deleted;
  }

  @Override
  public CloseableIterator<? extends StatisticValue<?>> getStatisticValues(
      Iterator<? extends Statistic<? extends StatisticValue<?>>> statistics,
      String... authorizations) {
    return new CloseableIterator<StatisticValue<?>>() {
      private CloseableIterator<? extends StatisticValue<?>> current = null;

      private StatisticValue<?> next = null;
      
      @SuppressWarnings("unchecked")
      private void computeNext() {
        if (next == null) {
          if (current != null && !current.hasNext()) {
            current.close();
            current = null;
          }
          while ((current == null || !current.hasNext()) && statistics.hasNext()) {
            Statistic<StatisticValue<Object>> nextStat = (Statistic<StatisticValue<Object>>) statistics.next();
            if (nextStat.getBinningStrategy() != null) {
              current = getBinnedStatisticValues(nextStat, authorizations);
            } else {
              StatisticValue<Object> value = getStatisticValue(nextStat, authorizations);
              if (value != null) {
                current = new CloseableIterator.Wrapper<>(Iterators.singletonIterator(value)); 
              }
            }
          }
          if (current != null && current.hasNext()) {
            next = current.next();
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
      public StatisticValue<?> next() {
        if (next == null) {
          computeNext();
        }
        StatisticValue<?> retVal = next;
        next = null;
        return retVal;
      }

      @Override
      public void close() {
      }
    };
  }
  
  @Override
  public <V extends StatisticValue<R>, R> V getStatisticValue(Statistic<V> statistic, String... authorizations) {
    if (statistic.getBinningStrategy() != null) {
      throw new UnsupportedOperationException("The given statistic uses a binning strategy, but no bin was specified.");
    }
    try (StatisticValueReader<V, R> reader = createStatisticValueReader(statistic, null)) {
      if (reader.hasNext()) {
        return reader.next();
      }
    }
    return null;
  }

  @Override
  public <V extends StatisticValue<R>, R> V getStatisticValue(Statistic<V> statistic, ByteArray bin, String... authorizations) {
    if (statistic.getBinningStrategy() == null) {
      throw new UnsupportedOperationException("The given statistic does not use a binning strategy, but a bin was specified.");
    }
    try (StatisticValueReader<V, R> reader = createStatisticValueReader(statistic, bin)) {
      if (reader.hasNext()) {
        return reader.next();
      }
    }
    return null;
  }
  
  @Override
  public <V extends StatisticValue<R>, R> CloseableIterator<BinnedStatisticValue<R>> getBinnedStatisticValues(Statistic<V> statistic, String... authorizations) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public <T> StatisticUpdateCallback<T> getUpdateCallback(DataTypeAdapter<T> adapter, Index index) {
    List<Statistic<? extends StatisticValue<?>>> statistics = Lists.newArrayList();
    try(CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> indexStats = getIndexStatistics(index, null, null)) {
      while (indexStats.hasNext()) {
        statistics.add(indexStats.next());
      }
    }
    try(CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> adapterStats = getAdapterStatistics(adapter, null, null)) {
      while (adapterStats.hasNext()) {
        statistics.add(adapterStats.next());
      }
    }
    return new StatisticUpdateCallback<>(statistics, this, index, adapter);
  }

  @Override
  public void removeAll() {
    super.removeAll();
  }
  
  @Override
  public boolean mergeStats() {
    return true;
  }

  public static class NameFilter implements CloseableIterator<Statistic<? extends StatisticValue<?>>> {

    private final CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> source;
    private final String name;

    private Statistic<? extends StatisticValue<?>> next = null;

    public NameFilter(CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> source, String name) {
      this.source = source;
      this.name = name;
    }

    private void computeNext() {
      while (source.hasNext()) {
        Statistic<? extends StatisticValue<?>> possibleNext = source.next();
        if (name.equals(possibleNext.getId().getName())) {
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
    public Statistic<? extends StatisticValue<?>> next() {
      if (next == null) {
        computeNext();
      }
      Statistic<? extends StatisticValue<?>> nextValue = next;
      next = null;
      return nextValue;
    }

    @Override
    public void close() {
      source.close();
    }

  }
  
  public static class FieldStatisticFilter implements CloseableIterator<Statistic<? extends StatisticValue<?>>> {

    private final CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> source;
    private final String fieldName;
    private final String name;

    private Statistic<? extends StatisticValue<?>> next = null;

    public FieldStatisticFilter(CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> source, String fieldName, String name) {
      this.source = source;
      this.fieldName = fieldName;
      this.name = name;
    }

    private void computeNext() {
      while (source.hasNext()) {
        Statistic<? extends StatisticValue<?>> possibleNext = source.next();
        if (possibleNext.getId() instanceof FieldStatisticId) {
          FieldStatisticId<? extends StatisticValue<?>> statisticId = (FieldStatisticId<? extends StatisticValue<?>>) possibleNext.getId();
          if ((name == null || statisticId.getName().equals(name)) &&
              (fieldName == null || statisticId.getFieldName().equals(fieldName))) {
            next = possibleNext;
            break;
          }
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
    public Statistic<? extends StatisticValue<?>> next() {
      if (next == null) {
        computeNext();
      }
      Statistic<? extends StatisticValue<?>> nextValue = next;
      next = null;
      return nextValue;
    }

    @Override
    public void close() {
      source.close();
    }

  }
}
