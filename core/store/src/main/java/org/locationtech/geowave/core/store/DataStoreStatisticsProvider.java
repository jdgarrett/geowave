/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store;

import java.util.Arrays;
import org.locationtech.geowave.core.store.adapter.InternalDataAdapter;
import org.locationtech.geowave.core.store.adapter.statistics.StatisticsProvider;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.data.visibility.DifferingFieldVisibilityEntryCount;
import org.locationtech.geowave.core.store.data.visibility.FieldVisibilityCount;
import org.locationtech.geowave.core.store.index.CommonIndexModel;
import org.locationtech.geowave.core.store.index.IndexMetaDataSet;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.index.DuplicateEntryCountStatistic;
import org.locationtech.geowave.core.store.statistics.index.PartitionsStatistic;
import org.locationtech.geowave.core.store.statistics.index.RowRangeHistogramStatistic;
import org.locationtech.geowave.core.store.statistics.visibility.EmptyStatisticVisibility;

// STATS_TODO: Default statistics need to be handled.
public class DataStoreStatisticsProvider<T> implements StatisticsProvider<T> {
  final InternalDataAdapter<T> adapter;
  final boolean includeAdapterStats;
  final Index index;

  public DataStoreStatisticsProvider(
      final InternalDataAdapter<T> adapter,
      final Index index,
      final boolean includeAdapterStats) {
    super();
    this.adapter = adapter;
    this.index = index;
    this.includeAdapterStats = includeAdapterStats;
  }

  @SuppressWarnings("unchecked")
  @Override
  public StatisticType[] getSupportedStatistics() {
    final StatisticType[] typesFromAdapter, typesFromIndex;
    if ((adapter.getAdapter() instanceof StatisticsProvider) && includeAdapterStats) {
      typesFromAdapter = ((StatisticsProvider<T>) adapter.getAdapter()).getSupportedStatistics();
    } else {
      typesFromAdapter = new StatisticType[0];
    }

    if (index != null) {
      final StatisticType[] newSet = Arrays.copyOf(typesFromAdapter, typesFromAdapter.length + 6);
      newSet[typesFromAdapter.length] = RowRangeHistogramStatistic.STATS_TYPE;
      newSet[typesFromAdapter.length + 1] = IndexMetaDataSet.STATS_TYPE;
      newSet[typesFromAdapter.length + 2] = DifferingFieldVisibilityEntryCount.STATS_TYPE;
      newSet[typesFromAdapter.length + 3] = FieldVisibilityCount.STATS_TYPE;
      newSet[typesFromAdapter.length + 4] = DuplicateEntryCountStatistic.STATS_TYPE;
      newSet[typesFromAdapter.length + 5] = PartitionsStatistic.STATS_TYPE;
      return newSet;
    }
    return typesFromAdapter;
  }

  @SuppressWarnings("unchecked")
  @Override
  public EntryVisibilityHandler<T> getVisibilityHandler(
      final CommonIndexModel indexModel,
      final DataTypeAdapter<T> adapter,
      final Statistic statistic) {
    return (adapter instanceof StatisticsProvider)
        ? ((StatisticsProvider<T>) adapter).getVisibilityHandler(
            index != null ? index.getIndexModel() : null,
            adapter,
            statistic)
        : new EmptyStatisticVisibility<>();
  }

  @Override
  public StatisticType[] getSupportedStatistics(String fieldName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Statistic[] getTrackedStatistics() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Statistic[] getTrackedStatistics(String fieldName) {
    // TODO Auto-generated method stub
    return null;
  }
}
