/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.adapter.vector.plugin.transaction;

import java.util.HashMap;
import java.util.Map;
import org.locationtech.geowave.adapter.vector.plugin.GeoWaveDataStoreComponents;
import org.locationtech.geowave.core.geotime.store.GeotoolsFeatureDataAdapter;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.CloseableIterator;
import org.locationtech.geowave.core.store.adapter.statistics.DataStatistics;
import org.locationtech.geowave.core.store.adapter.statistics.StatisticsId;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.statistics.DataStatisticsStore;
import org.opengis.feature.simple.SimpleFeature;

public abstract class AbstractTransactionManagement implements GeoWaveTransaction {

  protected final GeoWaveDataStoreComponents components;

  public AbstractTransactionManagement(final GeoWaveDataStoreComponents components) {
    super();
    this.components = components;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<ByteArray, Statistic<?>> getDataStatistics() {
    final Map<ByteArray, Statistic<?>> stats = new HashMap<>();
    final GeotoolsFeatureDataAdapter adapter = components.getAdapter();

    DataStatisticsStore statisticsStore = components.getStatsStore();
    addStats(stats, statisticsStore.getAdapterStatistics(adapter, null));
    return stats;
  }
  
  private void addStats(Map<ByteArray, Statistic<?>> statsMap, CloseableIterator<Statistic<?>> statistics) {
    while (statistics.hasNext()) {
      final Statistic<?> stat = statistics.next();
      statsMap.put(new ByteArray(stat.getId()), stat);
    }
  }
}
