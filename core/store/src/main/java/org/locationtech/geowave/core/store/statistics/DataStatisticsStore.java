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
import javax.annotation.Nullable;
import org.locationtech.geowave.core.store.CloseableIterator;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;

/**
 * This is responsible for persisting data statistics (either in memory or to disk depending on the
 * implementation).
 */
public interface DataStatisticsStore {
  /**
   * Get registered index statistics.
   * 
   * @return a list of index statistics
   */
  public List<Statistic<?>> getRegisteredIndexStatistics();

  /**
   * Get registered adapter statistics that are compatible with the the provided type.
   * 
   * @param type the type to get compatible statistics for
   * @return a list of compatible statistics
   */
  public List<Statistic<?>> getRegisteredAdapterStatistics(final Class<?> adapterDataClass);

  /**
   * Get registered field statistics that are compatible with the the provided type.
   * 
   * @param type the type to get compatible statistics for
   * @param fieldName the field to get compatible statistics for
   * @return a map of compatible statistics, keyed by field name
   */
  public Map<String, List<Statistic<?>>> getRegisteredFieldStatistics(
      final DataTypeAdapter<?> type,
      final @Nullable String fieldName);

  /**
   * Determines if the given statistic exists in the data store.
   * 
   * @param statistic the statistic to check for
   */
  public boolean exists(Statistic<?> statistic);

  /**
   * Add a statistic to the data store.
   * 
   * @param statistic the statistic to add
   */
  public void addStatistic(Statistic<?> statistic);

  /**
   * Remove a statistic from the data store.
   * 
   * @param statistic the statistic to remove
   * @param authorizations the authorizations for the query
   * @return {@code true} if the statistic existed and was removed
   */
  public boolean removeStatistic(Statistic<?> statistic);

  /**
   * Remove a set of statistics from the data store.
   * 
   * @param statistics the statistics to remove
   * @param authorizations the authorizations for the query
   * @return {@code true} if statistics were removed
   */
  public boolean removeStatistics(Iterator<Statistic<?>> statistics);

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
  public boolean removeStatistics(DataTypeAdapter<?> type);

  /**
   * This will write the statistic value to the underlying store. Note that this will overwrite
   * whatever the current persisted values are for the given statistic. Use incorporateStatistic to
   * aggregate the statistic value with any existing values.
   *
   * @param statistic The statistic to write
   */
  public void setStatistic(Statistic<?> statistic);

  /**
   * Add the statistic value to the store, overwriting existing values with the aggregation of this
   * value and the existing values
   *
   * @param statistic the statistic to incorporate
   */
  public void incorporateStatistic(Statistic<?> statistic);

  /**
   * Gets all of the tracked index statistics for the given index. If a type name is supplied, only
   * statistics that pertain to that type will be returned.
   * 
   * @param index the index to get statistics for
   * @param statisticType
   * @param typeName an optional type filter
   * @return a list of tracked statistics for the given index
   */
  public CloseableIterator<Statistic<?>> getIndexStatistics(
      final Index index,
      final @Nullable StatisticType statisticType,
      final @Nullable String typeName);

  /**
   * Gets all of the tracked adapter statistics for the given type.
   * 
   * @param type the type to get statistic for
   * @param statisticType an optional field name filter
   * @return a list of tracked statistics for the give type
   */
  public CloseableIterator<Statistic<?>> getAdapterStatistics(
      final DataTypeAdapter<?> type,
      final @Nullable StatisticType statisticType);

  /**
   * Gets all of the tracked field statistics for the given type. If a field name is specified, only
   * statistics that pertain to that field will be returned.
   * 
   * @param type
   * @param statisticType
   * @param fieldName
   * @return
   */
  public CloseableIterator<Statistic<?>> getFieldStatistics(
      final DataTypeAdapter<?> type,
      final @Nullable StatisticType statisticType,
      final @Nullable String fieldName);

  /**
   * Gets all of the tracked statistics in the data store.
   * 
   * @param statisticType an optional statistic type filter
   * @return a list of tracked statistics in the data store
   */
  public CloseableIterator<Statistic<?>> getAllStatistics(
      final @Nullable StatisticType statisticType);


  public CloseableIterator<? extends StatisticValue<?>> getStatisticValues(
      final Iterator<Statistic<?>> statistics,
      String... authorizations);

  /**
   * STATS_TODO: Could this return multiple values? How should multi-value statistics be handled?
   * 
   * @param <T>
   * @param statistic
   * @param authorizations
   * @return
   */
  public <T extends StatisticValue<?>> T getStatisticValue(
      final Statistic<T> statistic,
      String... authorizations);

  /**
   * Remove all statistics
   */
  public void removeAll();
}
