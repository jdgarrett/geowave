/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.DataStoreStatisticsProvider;
import org.locationtech.geowave.core.store.EntryVisibilityHandler;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticsQueryBuilder;
import org.locationtech.geowave.core.store.callback.DeleteCallback;
import org.locationtech.geowave.core.store.callback.IngestCallback;
import org.locationtech.geowave.core.store.callback.ScanCallback;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;

public class StatisticUpdateHandler<T, R> implements
    IngestCallback<T>,
    DeleteCallback<T, GeoWaveRow>,
    ScanCallback<T, GeoWaveRow> {
  private final Statistic<?> statistic;
  private final Map<ByteArray, StatisticUpdater> statisticsMap = new HashMap<>();
  private final EntryVisibilityHandler<T> visibilityHandler;
  private final DataTypeAdapter<T> adapter;

  public StatisticUpdateHandler(
      final Statistic<?> statistic,
      final Index index,
      final DataTypeAdapter<T> adapter) {
    this.statistic = statistic;
    this.adapter = adapter;
    this.visibilityHandler =
        statistic.getVisibilityHandler(index != null ? index.getIndexModel() : null, adapter);
  }

  public Map<ByteArray, StatisticUpdater> getStatisticValues() {
    return this.statisticsMap;
  }

  @Override
  public void entryIngested(final T entry, final GeoWaveRow... kvs) {
    final ByteArray visibility = new ByteArray(visibilityHandler.getVisibility(entry, kvs));
    StatisticUpdater updater = statisticsMap.get(visibility);
    if (updater == null) {
      updater = statistic.createEmpty();
      statisticsMap.put(visibility, updater);
    }
    if (updater instanceof StatisticsIngestCallback) {
      ((StatisticsIngestCallback) updater).entryIngested(adapter, entry, kvs);
    }
  }

  @Override
  public void entryDeleted(final T entry, final GeoWaveRow... kv) {
    final ByteArray visibility = new ByteArray(visibilityHandler.getVisibility(entry, kv));
    StatisticUpdater updater = statisticsMap.get(visibility);
    if (updater == null) {
      updater = statistic.createEmpty();
      statisticsMap.put(visibility, updater);
    }
    if (updater instanceof StatisticsDeleteCallback) {
      ((StatisticsDeleteCallback) updater).entryDeleted(adapter, entry, kv);
    }
  }

  @Override
  public void entryScanned(final T entry, final GeoWaveRow kv) {
    final ByteArray visibility = new ByteArray(visibilityHandler.getVisibility(entry, kv));
    StatisticUpdater updater = statisticsMap.get(visibility);
    if (updater == null) {
      updater = statistic.createEmpty();
      statisticsMap.put(visibility, updater);
    }
    if (updater instanceof StatisticsIngestCallback) {
      ((StatisticsIngestCallback) updater).entryIngested(adapter, entry, kv);
    }
  }
}
