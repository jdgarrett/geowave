/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.statistics;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.CloseableIterator;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticsRegistrySPI.RegisteredStatistic;
import com.google.common.collect.Maps;

/**
 * This is responsible for persisting data statistics (either in memory or to disk depending on the
 * implementation).
 */
public interface DataStatisticsStore {

  /**
   * Determines if the given statistic exists in the data store.
   * 
   * @param statistic the statistic to check for
   */
  public boolean exists(Statistic<? extends StatisticValue<?>> statistic);

  /**
   * Add a statistic to the data store.
   * 
   * @param statistic the statistic to add
   */
  public void addStatistic(Statistic<? extends StatisticValue<?>> statistic);

  /**
   * Remove a statistic from the data store.
   * 
   * @param statistic the statistic to remove
   * @param authorizations the authorizations for the query
   * @return {@code true} if the statistic existed and was removed
   */
  public boolean removeStatistic(Statistic<? extends StatisticValue<?>> statistic);

  /**
   * Remove a set of statistics from the data store.
   * 
   * @param statistics the statistics to remove
   * @param authorizations the authorizations for the query
   * @return {@code true} if statistics were removed
   */
  public boolean removeStatistics(
      Iterator<? extends Statistic<? extends StatisticValue<?>>> statistics);

  /**
   * Remove statistics associated with the given index.
   * 
   * @param index the index to remove statistics for
   * @return {@code true} if statistics were removed
   */
  public boolean removeStatistics(Index index);

  /**
   * Remove statistics associated with the given adapter.
   * 
   * @param type the type to remove statistics for
   * @return {@code true} if statistics were removed
   */
  public boolean removeStatistics(DataTypeAdapter<?> type, Index... adapterIndices);

  /**
   * Gets tracked index statistics for the given index.
   * 
   * @param index the index to get statistics for
   * @param statisticType an optional statistic type filter
   * @param name an optional name filter
   * @return a list of tracked statistics for the given index
   */
  public CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getIndexStatistics(
      final Index index,
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType,
      final @Nullable String tag);

  /**
   * Gets all of the tracked adapter statistics for the given type.
   * 
   * @param type the type to get statistic for
   * @param statisticType an optional statistic type filter
   * @param name an optional name filter
   * @return a list of tracked statistics for the give type
   */
  public CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getAdapterStatistics(
      final DataTypeAdapter<?> type,
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType,
      final @Nullable String tag);

  /**
   * Gets all of the tracked field statistics for the given type. If a field name is specified, only
   * statistics that pertain to that field will be returned.
   * 
   * @param type
   * @param statisticType
   * @param fieldName
   * @param name
   * @return
   */
  public CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getFieldStatistics(
      final DataTypeAdapter<?> type,
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType,
      final @Nullable String fieldName,
      final @Nullable String tag);

  /**
   * Gets all of the tracked statistics in the data store.
   * 
   * @param statisticType an optional statistic type filter
   * @return a list of tracked statistics in the data store
   */
  public CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> getAllStatistics(
      final @Nullable StatisticType<? extends StatisticValue<?>> statisticType);

  /**
   * Gets the statistic with the given StatisticId, or {@code null} if it could not be found.
   * 
   * @param statisticId the id of the statistic to get
   * @return
   */
  public <V extends StatisticValue<R>, R> Statistic<V> getStatisticById(
      final StatisticId<V> statisticId);

  /**
   * This will write the statistic value to the underlying store. Note that this will overwrite
   * whatever the current persisted values are for the given statistic. Use incorporateStatistic to
   * aggregate the statistic value with any existing values.
   *
   * @param statistic The statistic to write
   */
  public <V extends StatisticValue<R>, R> void setStatisticValue(Statistic<V> statistic, V value);

  /**
   * This will write the statistic value to the underlying store. Note that this will overwrite
   * whatever the current persisted values are for the given statistic. Use incorporateStatistic to
   * aggregate the statistic value with any existing values.
   *
   * @param statistic The statistic to write
   */
  public <V extends StatisticValue<R>, R> void setStatisticValue(
      Statistic<V> statistic,
      V value,
      ByteArray bin);

  /**
   * Add the statistic value to the store, overwriting existing values with the aggregation of this
   * value and the existing values
   *
   * @param statistic the statistic to incorporate
   */
  public <V extends StatisticValue<R>, R> void incorporateStatisticValue(
      Statistic<V> statistic,
      V value);

  /**
   * Add the statistic value to the store, overwriting existing values with the aggregation of this
   * value and the existing values
   *
   * @param statistic the statistic to incorporate
   */
  public <V extends StatisticValue<R>, R> void incorporateStatisticValue(
      Statistic<V> statistic,
      V value,
      ByteArray bin);

  public boolean removeStatisticValue(Statistic<? extends StatisticValue<?>> statistic);

  public boolean removeStatisticValue(
      Statistic<? extends StatisticValue<?>> statistic,
      ByteArray bin);

  public boolean removeStatisticValues(Statistic<? extends StatisticValue<?>> statistic);

  public <V extends StatisticValue<R>, R> StatisticValueWriter<V> createStatisticValueWriter(
      Statistic<V> statistic);

  public <T> StatisticUpdateCallback<T> createUpdateCallback(
      Index index,
      DataTypeAdapter<T> adapter,
      boolean updateAdapterStats);

  /**
   * Returns the values for each provided statistic. Only statistics that use a binning strategy and
   * match the given bins
   * 
   * @param statistics
   * @param authorizations
   * @return
   */
  public CloseableIterator<? extends StatisticValue<?>> getStatisticValues(
      final Iterator<? extends Statistic<? extends StatisticValue<?>>> statistics,
      final ByteArray[] bins,
      final String... authorizations);

  public <V extends StatisticValue<R>, R> V getStatisticValue(
      final Statistic<V> statistic,
      String... authorizations);

  public <V extends StatisticValue<R>, R> V getStatisticValue(
      final Statistic<V> statistic,
      ByteArray bin,
      String... authorizations);

  /**
   * Returns all of the values for a given statistic. If the statistic uses a binning strategy, each
   * bin will be returned as a separate value.
   * 
   * @param statistic the statistic to get values for
   * @param authorizations the authorizations
   * @return
   */
  public <V extends StatisticValue<R>, R> CloseableIterator<V> getStatisticValues(
      final Statistic<V> statistic,
      String... authorizations);

  public boolean mergeStats();

  /**
   * Remove all statistics
   */
  public void removeAll();
}
