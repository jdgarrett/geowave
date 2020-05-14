/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.adapter.statistics;

import java.util.Arrays;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import org.locationtech.geowave.core.store.adapter.statistics.StatisticsProviderSPI.StatisticsTypeAndConstructor;
import org.locationtech.geowave.core.store.api.StatisticsOptions;
import org.locationtech.geowave.core.store.cli.index.AddIndexCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Maps;

/**
 * Singleton registry for all supported statistics. Statistics can be added to the system using
 * {@link StatisticsProviderSPI}.
 */
public class StatisticsRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsRegistry.class);

  private static StatisticsRegistry INSTANCE = null;

  private Map<String, Supplier<StatisticsOptions>> statistics = Maps.newHashMap();

  private StatisticsRegistry() {
    final ServiceLoader<StatisticsProviderSPI> serviceLoader =
        ServiceLoader.load(StatisticsProviderSPI.class);
    for (final StatisticsProviderSPI providedStats : serviceLoader) {
      Arrays.stream(providedStats.getProvidedStatistics()).forEach(this::putStat);
    }
  }
  
  private void putStat(StatisticsTypeAndConstructor stat) {
    final String key = stat.getStatisticsType().getString().toLowerCase();
    if (statistics.containsKey(key)) {
      LOGGER.warn("Multiple statistics with the same type were found on the classpath. Only the first instance will be loaded!");
      return;
    }
    statistics.put(key, stat.getOptionsConstructor());
  }

  public static StatisticsRegistry instance() {
    if (INSTANCE == null) {
      INSTANCE = new StatisticsRegistry();
    }
    return INSTANCE;
  }

  /**
   * Retrieves the options class for the given statistics type.
   * 
   * @param statType the statistics type
   * @return the function that matches the given name, or {@code null} if it could not be found
   */
  public StatisticsOptions getStatisticsOptions(final StatisticsType statType) {
    Supplier<StatisticsOptions> optionsSupplier = statistics.get(statType.getString().toLowerCase());
    if (optionsSupplier == null) {
      return null;
    }
    return optionsSupplier.get();
  }
  
  /**
   * Retrieves the options class for the given statistics type.
   * 
   * @param statType the statistics type
   * @return the function that matches the given name, or {@code null} if it could not be found
   */
  public StatisticsOptions getStatisticsOptions(final String statType) {
    Supplier<StatisticsOptions> optionsSupplier = statistics.get(statType.toLowerCase());
    if (optionsSupplier == null) {
      return null;
    }
    return optionsSupplier.get();
  }

}
