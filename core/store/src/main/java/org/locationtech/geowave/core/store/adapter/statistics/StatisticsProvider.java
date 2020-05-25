/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.adapter.statistics;

import org.locationtech.geowave.core.store.EntryVisibilityHandler;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.index.CommonIndexModel;
import org.locationtech.geowave.core.store.statistics.StatisticType;

/**
 * This interface defines the set of statistics to capture for a specific adapter.
 *
 * @param <T> The type for the data elements that are being adapted by the adapter
 */
public interface StatisticsProvider<T> {
  public StatisticType[] getSupportedStatistics();

  public StatisticType[] getSupportedStatistics(final String fieldName);

  public Statistic[] getTrackedStatistics();

  public Statistic[] getTrackedStatistics(final String fieldName);

  public EntryVisibilityHandler<T> getVisibilityHandler(
      CommonIndexModel indexModel,
      DataTypeAdapter<T> adapter,
      Statistic statistic);
}
