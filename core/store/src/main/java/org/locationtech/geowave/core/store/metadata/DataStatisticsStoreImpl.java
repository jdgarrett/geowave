package org.locationtech.geowave.core.store.metadata;

import java.util.Arrays;
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
import org.locationtech.geowave.core.store.operations.MetadataQuery.PrimaryIdQueryType;
import org.locationtech.geowave.core.store.operations.MetadataType;
import org.locationtech.geowave.core.store.statistics.AdapterBinningStrategy;
import org.locationtech.geowave.core.store.statistics.CompositeBinningStrategy;
import org.locationtech.geowave.core.store.statistics.DataStatisticsStore;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.StatisticUpdateCallback;
import org.locationtech.geowave.core.store.statistics.StatisticValueReader;
import org.locationtech.geowave.core.store.statistics.StatisticValueWriter;
import org.locationtech.geowave.core.store.statistics.StatisticsRegistry;
import org.locationtech.geowave.core.store.statistics.StatisticsRegistrySPI.RegisteredStatistic;
import org.locationtech.geowave.core.store.statistics.adapter.AdapterStatistic;
import org.locationtech.geowave.core.store.statistics.field.FieldStatistic;
import org.locationtech.geowave.core.store.statistics.field.FieldStatisticId;
import org.locationtech.geowave.core.store.statistics.index.IndexStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DataStatisticsStoreImpl extends
    AbstractGeoWavePersistence<Statistic<? extends StatisticValue<?>>> implements
    DataStatisticsStore {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataStatisticsStoreImpl.class);
  // this is fairly arbitrary at the moment because it is the only custom
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
    return persistedObject.getId().getGroupId();
  }

  @Override
  public List<? extends Statistic<? extends StatisticValue<?>>> getRegisteredIndexStatistics() {
    return StatisticsRegistry.instance().getStatistics().values().stream().filter(
        RegisteredStatistic::isIndexStatistic).map(s -> s.getOptionsConstructor().get()).collect(
            Collectors.toList());
  }

  @Override
  public List<? extends Statistic<? extends StatisticValue<?>>> getRegisteredAdapterStatistics(
      Class<?> adapterDataClass) {
    return StatisticsRegistry.instance().getStatistics().values().stream().filter(
        s -> s.isAdapterStatistic() && s.isCompatibleWith(adapterDataClass)).map(
            s -> s.getOptionsConstructor().get()).collect(Collectors.toList());
  }

  @Override
  public Map<String, List<? extends Statistic<? extends StatisticValue<?>>>> getRegisteredFieldStatistics(
      DataTypeAdapter<?> type,
      String fieldName) {
    Map<String, List<? extends Statistic<? extends StatisticValue<?>>>> statistics =
        Maps.newHashMap();
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
    // STATS_TODO: There needs to be a way to validate the statistic (maybe a statistic.validate()
    // function that throws an exception)
    this.addObject(statistic);
  }

  @Override
  public boolean removeStatistic(Statistic<? extends StatisticValue<?>> statistic) {
    // Delete the statistic values
    removeStatisticValues(statistic);
    return deleteObject(getPrimaryId(statistic), getSecondaryId(statistic));
  }

  @Override
  public boolean removeStatistics(
      Iterator<? extends Statistic<? extends StatisticValue<?>>> statistics) {
    boolean deleted = false;
    while (statistics.hasNext()) {
      Statistic<? extends StatisticValue<?>> statistic = statistics.next();
      removeStatisticValues(statistic);
      deleted = deleted || deleteObject(getPrimaryId(statistic), getSecondaryId(statistic));
    }
    return deleted;
  }

  @Override
  public boolean removeStatistics(final Index index) {
    return removeStatistics(getIndexStatistics(index, null, null));
  }

  @Override
  public boolean removeStatistics(final DataTypeAdapter<?> type, final Index... adapterIndices) {
    boolean removed = deleteObjects(AdapterStatistic.generateGroupId(type.getTypeName()));
    removed = removed || deleteObjects(null, AdapterStatistic.generateGroupId(type.getTypeName()), operations, MetadataType.STAT_VALUES, this);
    removed = removed || deleteObjects(FieldStatistic.generateGroupId(type.getTypeName()));
    removed = removed || deleteObjects(null, FieldStatistic.generateGroupId(type.getTypeName()), operations, MetadataType.STAT_VALUES, this);
    final ByteArray adapterBin = AdapterBinningStrategy.getBin(type);
    for (Index index : adapterIndices) {
      try (CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> statsIter =
          getIndexStatistics(index, null, null)) {
        while (statsIter.hasNext()) {
          Statistic<?> next = statsIter.next();
          deleteAdapterIndexStatisticValues(next, adapterBin);
        }
      }
    }
    return removed;
  }

  @SuppressWarnings("unchecked")
  private void deleteAdapterIndexStatisticValues(
      Statistic<?> indexStatistic,
      ByteArray adapterBin) {
    if (indexStatistic.getBinningStrategy() instanceof AdapterBinningStrategy) {
      removeStatisticValue(indexStatistic, adapterBin);
    } else if (indexStatistic.getBinningStrategy() instanceof CompositeBinningStrategy
        && ((CompositeBinningStrategy) indexStatistic.getBinningStrategy()).usesStrategy(
            AdapterBinningStrategy.class)) {
      int strategyIndex =
          ((CompositeBinningStrategy) indexStatistic.getBinningStrategy()).getStrategyIndex(
              AdapterBinningStrategy.class);
      // TODO: The current metadata deleter only deletes exact values. One future optimization
      // could be to allow it to delete with a primary Id prefix. If the strategy index is 0,
      // a prefix delete could be used.
      List<ByteArray> binsToRemove = Lists.newLinkedList();
      try (CloseableIterator<StatisticValue<Object>> valueIter =
          getStatisticValues((Statistic<StatisticValue<Object>>) indexStatistic)) {
        while (valueIter.hasNext()) {
          ByteArray bin = valueIter.next().getBin();
          if (CompositeBinningStrategy.tokenMatches(bin, strategyIndex, adapterBin)) {
            binsToRemove.add(bin);
          }
        }
      }
      for (ByteArray bin : binsToRemove) {
        removeStatisticValue(indexStatistic, bin);
      }
    }
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
      final @Nullable String tag) {
    if (statisticType == null) {
      CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> stats =
          getAllObjectsWithSecondaryId(secondaryId);
      if (tag == null) {
        return stats;
      }
      return new TagFilter(stats, tag);
    } else if (tag == null) {
      return internalGetObjects(
          new MetadataQuery(
              statisticType.getBytes(),
              secondaryId.getBytes(),
              PrimaryIdQueryType.PREFIX));
    }
    return getCachedObject(StatisticId.generateUniqueId(statisticType, tag), secondaryId);
  }

  protected CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getFieldStatisticsInternal(
      final ByteArray secondaryId,
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType,
      final @Nullable String fieldName,
      final @Nullable String tag) {
    if (statisticType != null) {
      if (fieldName != null) {
        ByteArray primaryId = FieldStatisticId.generateUniqueId(statisticType, fieldName, tag);
        if (tag != null) {
          return getCachedObject(primaryId, secondaryId);
        } else {
          return internalGetObjects(
              new MetadataQuery(
                  primaryId.getBytes(),
                  secondaryId.getBytes(),
                  PrimaryIdQueryType.PREFIX));
        }
      } else {
        if (tag != null) {
          return new TagFilter(
              internalGetObjects(
                  new MetadataQuery(
                      statisticType.getBytes(),
                      secondaryId.getBytes(),
                      PrimaryIdQueryType.PREFIX)),
              tag);
        } else {
          return internalGetObjects(
              new MetadataQuery(
                  statisticType.getBytes(),
                  secondaryId.getBytes(),
                  PrimaryIdQueryType.PREFIX));
        }
      }
    }
    return new FieldStatisticFilter(getAllObjectsWithSecondaryId(secondaryId), fieldName, tag);
  }

  protected CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getAllStatisticsInternal(
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType) {
    return internalGetObjects(
        new MetadataQuery(
            statisticType == null ? null : statisticType.getBytes(),
            null,
            PrimaryIdQueryType.PREFIX));
  }

  @Override
  public CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getIndexStatistics(
      final Index index,
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType,
      final @Nullable String tag) {
    return getBasicStatisticsInternal(
        IndexStatistic.generateGroupId(index.getName()),
        statisticType,
        tag);
  }

  @Override
  public CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getAdapterStatistics(
      final DataTypeAdapter<?> type,
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType,
      final @Nullable String tag) {
    return getBasicStatisticsInternal(
        AdapterStatistic.generateGroupId(type.getTypeName()),
        statisticType,
        tag);
  }

  @Override
  public CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getFieldStatistics(
      final DataTypeAdapter<?> type,
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType,
      final @Nullable String fieldName,
      final @Nullable String tag) {
    return getFieldStatisticsInternal(
        FieldStatistic.generateGroupId(type.getTypeName()),
        statisticType,
        fieldName,
        tag);

  }

  @SuppressWarnings("unchecked")
  @Override
  public CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getAllStatistics(
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType) {
    return (CloseableIterator<Statistic<StatisticValue<Object>>>) getAllStatisticsInternal(
        statisticType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends StatisticValue<R>, R> Statistic<V> getStatisticById(
      StatisticId<V> statisticId) {
    try (CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> iterator =
        getCachedObject(statisticId.getUniqueId(), statisticId.getGroupId())) {
      if (iterator.hasNext()) {
        return (Statistic<V>) iterator.next();
      }
    }
    return null;
  }


  @Override
  public <V extends StatisticValue<R>, R> void setStatisticValue(Statistic<V> statistic, V value) {
    if (statistic.getBinningStrategy() != null) {
      throw new UnsupportedOperationException(
          "The given statistic uses a binning strategy, but no bin was specified.");
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
      throw new UnsupportedOperationException(
          "The given statistic does not use a binning strategy, but a bin was specified.");
    }
    removeStatisticValue(statistic, bin);
    incorporateStatisticValue(statistic, value, bin);
  }

  @Override
  public <V extends StatisticValue<R>, R> void incorporateStatisticValue(
      Statistic<V> statistic,
      V value) {
    if (statistic.getBinningStrategy() != null) {
      throw new UnsupportedOperationException(
          "The given statistic uses a binning strategy, but no bin was specified.");
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
      throw new UnsupportedOperationException(
          "The given statistic does not use a binning strategy, but a bin was specified.");
    }
    try (StatisticValueWriter<V> writer = createStatisticValueWriter(statistic)) {
      writer.writeStatisticValue(bin.getBytes(), null, value);
    } catch (Exception e) {
      LOGGER.error("Unable to write statistic value", e);
    }
  }


  @Override
  public <V extends StatisticValue<R>, R> StatisticValueWriter<V> createStatisticValueWriter(
      Statistic<V> statistic) {
    return new StatisticValueWriter<>(
        operations.createMetadataWriter(MetadataType.STAT_VALUES),
        statistic);
  }

  private <V extends StatisticValue<R>, R> StatisticValueReader<V, R> createStatisticValueReader(
      Statistic<V> statistic,
      ByteArray bin,
      boolean exact,
      String... authorizations) {
    MetadataQuery query =
        new MetadataQuery(
            StatisticValue.getValueId(statistic.getId(), bin),
            statistic.getId().getGroupId().getBytes(),
            exact ? PrimaryIdQueryType.EXACT : PrimaryIdQueryType.PREFIX,
            authorizations);
    return new StatisticValueReader<>(
        operations.createMetadataReader(MetadataType.STAT_VALUES).query(query),
        statistic);
  }

  @Override
  public boolean removeStatisticValue(Statistic<? extends StatisticValue<?>> statistic) {
    if (statistic.getBinningStrategy() != null) {
      throw new UnsupportedOperationException(
          "The given statistic uses a binning strategy, but no bin was specified.");
    }
    boolean deleted = false;
    try (MetadataDeleter deleter = operations.createMetadataDeleter(MetadataType.STAT_VALUES)) {
      deleted =
          deleter.delete(
              new MetadataQuery(
                  StatisticValue.getValueId(statistic.getId(), null),
                  statistic.getId().getGroupId().getBytes()));
    } catch (Exception e) {
      LOGGER.error("Unable to remove value for statistic", e);
    }
    return deleted;
  }

  @Override
  public boolean removeStatisticValue(
      Statistic<? extends StatisticValue<?>> statistic,
      ByteArray bin) {
    if (statistic.getBinningStrategy() == null) {
      throw new UnsupportedOperationException(
          "The given statistic does not use a binning strategy, but a bin was specified.");
    }
    boolean deleted = false;
    try (MetadataDeleter deleter = operations.createMetadataDeleter(MetadataType.STAT_VALUES)) {
      deleted =
          deleter.delete(
              new MetadataQuery(
                  StatisticValue.getValueId(statistic.getId(), bin),
                  statistic.getId().getGroupId().getBytes()));
    } catch (Exception e) {
      LOGGER.error("Unable to remove value for statistic", e);
    }
    return deleted;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean removeStatisticValues(Statistic<? extends StatisticValue<?>> statistic) {
    if (statistic.getBinningStrategy() == null) {
      return removeStatisticValue(statistic);
    }
    // TODO: The performance of this operation could be improved if primary ID prefix queries were
    // allowed during delete.
    boolean deleted = false;
    List<ByteArray> binsToRemove = Lists.newLinkedList();
    try (CloseableIterator<StatisticValue<Object>> valueIter =
        getStatisticValues((Statistic<StatisticValue<Object>>) statistic)) {
      while (valueIter.hasNext()) {
        ByteArray bin = valueIter.next().getBin();
        binsToRemove.add(bin);
      }
    }
    for (ByteArray bin : binsToRemove) {
      deleted = deleted || removeStatisticValue(statistic, bin);
    }
    return deleted;
  }

  @Override
  public CloseableIterator<? extends StatisticValue<?>> getStatisticValues(
      final Iterator<? extends Statistic<? extends StatisticValue<?>>> statistics,
      final ByteArray[] bins,
      final String... authorizations) {
    return new CloseableIterator<StatisticValue<?>>() {
      private CloseableIterator<? extends StatisticValue<?>> current = null;

      private StatisticValue<?> next = null;

      @SuppressWarnings("unchecked")
      private void computeNext() {
        if (next == null) {
          while ((current == null || !current.hasNext()) && statistics.hasNext()) {
            if (current != null) {
              current.close();
              current = null;
            }
            Statistic<StatisticValue<Object>> nextStat =
                (Statistic<StatisticValue<Object>>) statistics.next();
            if (nextStat.getBinningStrategy() != null && bins != null && bins.length > 0) {
              current =
                  new CloseableIterator.Wrapper<>(
                      Arrays.stream(bins).map(
                          bin -> getStatisticValue(nextStat, bin, authorizations)).iterator());
            } else {
              current = getStatisticValues(nextStat, authorizations);
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
        if (current != null) {
          current.close();
          current = null;
        }
      }
    };
  }

  @Override
  public <V extends StatisticValue<R>, R> V getStatisticValue(
      Statistic<V> statistic,
      String... authorizations) {
    if (statistic.getBinningStrategy() != null) {
      throw new UnsupportedOperationException(
          "The given statistic uses a binning strategy, but no bin was specified.");
    }
    try (StatisticValueReader<V, R> reader =
        createStatisticValueReader(statistic, null, true, authorizations)) {
      if (reader.hasNext()) {
        return reader.next();
      }
    }
    return null;
  }

  @Override
  public <V extends StatisticValue<R>, R> V getStatisticValue(
      Statistic<V> statistic,
      ByteArray bin,
      String... authorizations) {
    if (statistic.getBinningStrategy() == null) {
      throw new UnsupportedOperationException(
          "The given statistic does not use a binning strategy, but a bin was specified.");
    }
    try (StatisticValueReader<V, R> reader =
        createStatisticValueReader(statistic, bin, true, authorizations)) {
      if (reader.hasNext()) {
        return reader.next();
      }
    }
    return null;
  }

  @Override
  public <V extends StatisticValue<R>, R> CloseableIterator<V> getStatisticValues(
      Statistic<V> statistic,
      String... authorizations) {
    if (statistic.getBinningStrategy() != null) {
      return createStatisticValueReader(statistic, null, false, authorizations);
    }
    return createStatisticValueReader(statistic, null, true, authorizations);
  }

  @Override
  public <T> StatisticUpdateCallback<T> createUpdateCallback(
      Index index,
      DataTypeAdapter<T> adapter,
      boolean updateAdapterStats) {
    List<Statistic<? extends StatisticValue<?>>> statistics = Lists.newArrayList();
    try (CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> indexStats =
        getIndexStatistics(index, null, null)) {
      while (indexStats.hasNext()) {
        statistics.add(indexStats.next());
      }
    }
    if (updateAdapterStats) {
      try (CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> adapterStats =
          getAdapterStatistics(adapter, null, null)) {
        while (adapterStats.hasNext()) {
          statistics.add(adapterStats.next());
        }
      }
      try (CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> fieldStats =
          getFieldStatistics(adapter, null, null, null)) {
        while (fieldStats.hasNext()) {
          statistics.add(fieldStats.next());
        }
      }
    }
    return new StatisticUpdateCallback<>(statistics, this, index, adapter);
  }

  @Override
  public void removeAll() {
    deleteObjects(null, null, operations, MetadataType.STAT_VALUES, null);
    super.removeAll();
  }

  @Override
  public boolean mergeStats() {
    // STATS_TODO: implement
    return true;
  }

  protected static class TagFilter implements
      CloseableIterator<Statistic<? extends StatisticValue<?>>> {

    private final CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> source;
    private final String tag;

    private Statistic<? extends StatisticValue<?>> next = null;

    public TagFilter(
        CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> source,
        String tag) {
      this.source = source;
      this.tag = tag;
    }

    private void computeNext() {
      while (source.hasNext()) {
        Statistic<? extends StatisticValue<?>> possibleNext = source.next();
        if (tag.equals(possibleNext.getId().getTag())) {
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

  protected static class FieldStatisticFilter implements
      CloseableIterator<Statistic<? extends StatisticValue<?>>> {

    private final CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> source;
    private final String fieldName;
    private final String tag;

    private Statistic<? extends StatisticValue<?>> next = null;

    public FieldStatisticFilter(
        CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> source,
        String fieldName,
        String tag) {
      this.source = source;
      this.fieldName = fieldName;
      this.tag = tag;
    }

    private void computeNext() {
      while (source.hasNext()) {
        Statistic<? extends StatisticValue<?>> possibleNext = source.next();
        if (possibleNext.getId() instanceof FieldStatisticId) {
          FieldStatisticId<? extends StatisticValue<?>> statisticId =
              (FieldStatisticId<? extends StatisticValue<?>>) possibleNext.getId();
          if ((tag == null || statisticId.getTag().equals(tag))
              && (fieldName == null || statisticId.getFieldName().equals(fieldName))) {
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
