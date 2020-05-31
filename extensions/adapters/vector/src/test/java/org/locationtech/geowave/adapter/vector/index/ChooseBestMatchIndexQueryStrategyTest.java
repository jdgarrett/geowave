/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.adapter.vector.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import org.locationtech.geowave.core.geotime.index.api.SpatialIndexBuilder;
import org.locationtech.geowave.core.geotime.index.api.SpatialTemporalIndexBuilder;
import org.locationtech.geowave.core.geotime.index.dimension.LatitudeDefinition;
import org.locationtech.geowave.core.geotime.index.dimension.LongitudeDefinition;
import org.locationtech.geowave.core.geotime.index.dimension.TimeDefinition;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.InsertionIds;
import org.locationtech.geowave.core.index.NumericIndexStrategy;
import org.locationtech.geowave.core.index.SinglePartitionInsertionIds;
import org.locationtech.geowave.core.index.sfc.data.BasicNumericDataset;
import org.locationtech.geowave.core.index.sfc.data.NumericData;
import org.locationtech.geowave.core.index.sfc.data.NumericRange;
import org.locationtech.geowave.core.index.sfc.data.NumericValue;
import org.locationtech.geowave.core.store.CloseableIterator;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.dimension.NumericDimensionField;
import org.locationtech.geowave.core.store.entities.GeoWaveKeyImpl;
import org.locationtech.geowave.core.store.entities.GeoWaveRowImpl;
import org.locationtech.geowave.core.store.entities.GeoWaveValue;
import org.locationtech.geowave.core.store.index.CommonIndexValue;
import org.locationtech.geowave.core.store.index.NullIndex;
import org.locationtech.geowave.core.store.query.constraints.BasicQueryByClass;
import org.locationtech.geowave.core.store.query.constraints.BasicQueryByClass.ConstraintData;
import org.locationtech.geowave.core.store.query.constraints.BasicQueryByClass.ConstraintSet;
import org.locationtech.geowave.core.store.query.constraints.BasicQueryByClass.ConstraintsByClass;
import org.locationtech.geowave.core.store.statistics.BinnedStatisticValue;
import org.locationtech.geowave.core.store.statistics.DataStatisticsStore;
import org.locationtech.geowave.core.store.statistics.PartitionBinningStrategy;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.StatisticValueWriter;
import org.locationtech.geowave.core.store.statistics.StatisticsIngestCallback;
import org.locationtech.geowave.core.store.statistics.index.IndexStatistic;
import org.locationtech.geowave.core.store.statistics.index.RowRangeHistogramStatistic;
import org.locationtech.geowave.core.store.statistics.index.RowRangeHistogramStatistic.RowRangeHistogramValue;
import org.spark_project.guava.collect.Iterators;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;

public class ChooseBestMatchIndexQueryStrategyTest {
  final Index IMAGE_CHIP_INDEX1 = new NullIndex("IMAGERY_CHIPS1");
  final Index IMAGE_CHIP_INDEX2 = new NullIndex("IMAGERY_CHIPS2");
  private static long SEED = 12345;
  private static long ROWS = 1000000;

  @Test
  public void testChooseSpatialTemporalWithStats() {
    final Index temporalindex = new SpatialTemporalIndexBuilder().createIndex();
    final Index spatialIndex = new SpatialIndexBuilder().createIndex();

    final RowRangeHistogramStatistic rangeTempStats =
        new RowRangeHistogramStatistic(temporalindex.getName());
    rangeTempStats.setBinningStrategy(new PartitionBinningStrategy());

    final RowRangeHistogramStatistic rangeStats =
        new RowRangeHistogramStatistic(spatialIndex.getName());
    rangeStats.setBinningStrategy(new PartitionBinningStrategy());

    final Map<StatisticId<?>, Map<ByteArray, StatisticValue<?>>> statsMap = new HashMap<>();

    final ChooseBestMatchIndexQueryStrategy strategy = new ChooseBestMatchIndexQueryStrategy();

    final ConstraintSet cs1 = new ConstraintSet();
    cs1.addConstraint(
        LatitudeDefinition.class,
        new ConstraintData(new ConstrainedIndexValue(0.3, 0.5), true));

    cs1.addConstraint(
        LongitudeDefinition.class,
        new ConstraintData(new ConstrainedIndexValue(0.4, 0.7), true));

    final ConstraintSet cs2a = new ConstraintSet();
    cs2a.addConstraint(
        TimeDefinition.class,
        new ConstraintData(new ConstrainedIndexValue(0.1, 0.2), true));

    final ConstraintsByClass constraints =
        new ConstraintsByClass(Arrays.asList(cs2a)).merge(Collections.singletonList(cs1));

    final BasicQueryByClass query = new BasicQueryByClass(constraints);

    final NumericIndexStrategy temporalIndexStrategy =
        new SpatialTemporalIndexBuilder().createIndex().getIndexStrategy();
    final Random r = new Random(SEED);
    for (int i = 0; i < ROWS; i++) {
      final double x = r.nextDouble();
      final double y = r.nextDouble();
      final double t = r.nextDouble();
      final InsertionIds id =
          temporalIndexStrategy.getInsertionIds(
              new BasicNumericDataset(
                  new NumericData[] {
                      new NumericValue(x),
                      new NumericValue(y),
                      new NumericValue(t)}));
      for (final SinglePartitionInsertionIds range : id.getPartitionKeys()) {
        Map<ByteArray, StatisticValue<?>> binValues = statsMap.get(rangeTempStats.getId());
        if (binValues == null) {
          binValues = Maps.newHashMap();
          statsMap.put(rangeTempStats.getId(), binValues);
        }
        RowRangeHistogramValue value = (RowRangeHistogramValue) binValues.get(new ByteArray(range.getPartitionKey()));
        if (value == null) {
          value = rangeTempStats.createEmpty();
          binValues.put(new ByteArray(range.getPartitionKey()), value);
        }
        ((StatisticsIngestCallback) value).entryIngested(
            null,
            null,
            new GeoWaveRowImpl(
                new GeoWaveKeyImpl(
                    new byte[] {1},
                    (short) 1,
                    range.getPartitionKey(),
                    range.getSortKeys().get(0),
                    0),
                new GeoWaveValue[] {}));
      }
    }
    final Index index = new SpatialIndexBuilder().createIndex();
    final NumericIndexStrategy indexStrategy = index.getIndexStrategy();

    for (int i = 0; i < ROWS; i++) {
      final double x = r.nextDouble();
      final double y = r.nextDouble();
      final double t = r.nextDouble();
      final InsertionIds id =
          indexStrategy.getInsertionIds(
              new BasicNumericDataset(
                  new NumericData[] {
                      new NumericValue(x),
                      new NumericValue(y),
                      new NumericValue(t)}));
      for (final SinglePartitionInsertionIds range : id.getPartitionKeys()) {
        Map<ByteArray, StatisticValue<?>> binValues = statsMap.get(rangeStats.getId());
        if (binValues == null) {
          binValues = Maps.newHashMap();
          statsMap.put(rangeStats.getId(), binValues);
        }
        RowRangeHistogramValue value = (RowRangeHistogramValue) binValues.get(new ByteArray(range.getPartitionKey()));
        if (value == null) {
          value = rangeStats.createEmpty();
          binValues.put(new ByteArray(range.getPartitionKey()), value);
        }
        ((StatisticsIngestCallback) value).entryIngested(
            null,
            null,
            new GeoWaveRowImpl(
                new GeoWaveKeyImpl(
                    new byte[] {1},
                    (short) 1,
                    range.getPartitionKey(),
                    range.getSortKeys().get(0),
                    0),
                new GeoWaveValue[] {}));
      }
    }

    final Iterator<Index> it = getIndices(new TestDataStatisticsStore(Lists.newArrayList(rangeStats, rangeTempStats), statsMap), query, strategy);
    assertTrue(it.hasNext());
    assertEquals(temporalindex.getName(), it.next().getName());
    assertFalse(it.hasNext());
  }

  public Iterator<Index> getIndices(
      final DataStatisticsStore statisticsStore,
      final BasicQueryByClass query,
      final ChooseBestMatchIndexQueryStrategy strategy) {
    return strategy.getIndices(
        statisticsStore,
        query,
        new Index[] {
            IMAGE_CHIP_INDEX1,
            new SpatialTemporalIndexBuilder().createIndex(),
            new SpatialIndexBuilder().createIndex(),
            IMAGE_CHIP_INDEX2},
        null,
        Maps.newHashMap());
  }

  public static class ConstrainedIndexValue extends NumericRange implements CommonIndexValue {

    /** */
    private static final long serialVersionUID = 1L;

    public ConstrainedIndexValue(final double min, final double max) {
      super(min, max);
      //
    }

    @Override
    public byte[] getVisibility() {
      return new byte[0];
    }

    @Override
    public void setVisibility(final byte[] visibility) {}

    @Override
    public boolean overlaps(final NumericDimensionField[] field, final NumericData[] rangeData) {
      return false;
    }
  }
  
  public static class TestDataStatisticsStore implements DataStatisticsStore {
    
    private List<Statistic<?>> statistics;
    private Map<StatisticId<?>, Map<ByteArray, StatisticValue<?>>> statisticValues;
    
    public TestDataStatisticsStore(List<Statistic<?>> statistics, Map<StatisticId<?>, Map<ByteArray, StatisticValue<?>>> statisticValues) {
      this.statistics = statistics;
      this.statisticValues = statisticValues;
    }

    @Override
    public List<Statistic<?>> getRegisteredIndexStatistics() {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<Statistic<?>> getRegisteredAdapterStatistics(Class<?> adapterDataClass) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, List<Statistic<?>>> getRegisteredFieldStatistics(
        DataTypeAdapter<?> type,
        String fieldName) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(Statistic<?> statistic) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addStatistic(Statistic<?> statistic) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeStatistic(Statistic<?> statistic) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeStatistics(Iterator<Statistic<?>> statistics) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeStatistics(Index index) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeStatistics(DataTypeAdapter<?> type) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CloseableIterator<Statistic<?>> getIndexStatistics(
        Index index,
        StatisticType<?> statisticType,
        String name) {
      return new CloseableIterator.Wrapper<>(
          statistics.stream().filter(
              stat -> stat instanceof IndexStatistic
                  && ((IndexStatistic<?>) stat).getIndexName().equals(index.getName())
                  && (statisticType == null || statisticType.equals(stat.getStatisticType()))
                  && (name == null || name.equals(stat.getName()))).iterator());
    }

    @Override
    public CloseableIterator<Statistic<?>> getAdapterStatistics(
        DataTypeAdapter<?> type,
        StatisticType<?> statisticType,
        String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CloseableIterator<Statistic<?>> getFieldStatistics(
        DataTypeAdapter<?> type,
        StatisticType<?> statisticType,
        String fieldName,
        String name) {
      throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StatisticValue<?>> Statistic<V> getStatisticById(StatisticId<V> statisticId) {
      return (Statistic<V>) statistics.stream().filter(s -> s.getId().equals(statisticId)).findFirst().orElse(null);
    }

    @Override
    public CloseableIterator<Statistic<?>> getAllStatistics(StatisticType<?> statisticType) {
      return new CloseableIterator.Wrapper<>(statistics.stream().filter(stat -> stat.getStatisticType().equals(statisticType)).iterator());
    }

    @Override
    public CloseableIterator<? extends StatisticValue<?>> getStatisticValues(
        Iterator<Statistic<?>> statistics,
        String... authorizations) {
      throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends StatisticValue<?>> R getStatisticValue(
        Statistic<R> statistic,
        ByteArray bin,
        String... authorizations) {
      Map<ByteArray, StatisticValue<?>> values = statisticValues.get(statistic.getId());
      if (values != null) {
        return (R) values.get(bin);
      }
      return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StatisticValue<R>, R> CloseableIterator<BinnedStatisticValue<R>> getBinnedStatisticValues(
        Statistic<V> statistic,
        String... authorizations) {
      Map<ByteArray, StatisticValue<?>> values = statisticValues.get(statistic.getId());
      if (values != null) {
        return new CloseableIterator.Wrapper<>(
            Iterators.transform(
                values.entrySet().iterator(),
                kv -> new BinnedStatisticValue<>((V) kv.getValue(), kv.getKey())));
      }
      return new CloseableIterator.Empty<>();
    }

    @Override
    public <R extends StatisticValue<?>> R getStatisticValue(
        Statistic<R> statistic,
        String... authorizations) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void removeAll() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <V extends StatisticValue<?>> void setStatisticValue(Statistic<V> statistic, V value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <V extends StatisticValue<?>> void setStatisticValue(
        Statistic<V> statistic,
        V value,
        ByteArray bin) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <V extends StatisticValue<?>> void incorporateStatisticValue(
        Statistic<V> statistic,
        V value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <V extends StatisticValue<?>> void incorporateStatisticValue(
        Statistic<V> statistic,
        V value,
        ByteArray bin) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeStatisticValue(Statistic<?> statistic) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeStatisticValue(Statistic<?> statistic, ByteArray bin) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <V extends StatisticValue<?>> StatisticValueWriter<V> createStatisticValueWriter(
        Statistic<V> statistic) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean mergeStats() {
      // TODO Auto-generated method stub
      return false;
    }
    
  }
}
