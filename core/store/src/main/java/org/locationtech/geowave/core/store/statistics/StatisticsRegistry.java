/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.statistics;

import java.util.Arrays;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticBinningStrategy;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticsRegistrySPI.RegisteredBinningStrategy;
import org.locationtech.geowave.core.store.statistics.StatisticsRegistrySPI.RegisteredStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Maps;

/**
 * Singleton registry for all supported statistics. Statistics can be added to the system using
 * {@link StatisticsRegistrySPI}.
 */
public class StatisticsRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsRegistry.class);

  private static StatisticsRegistry INSTANCE = null;

  private Map<String, RegisteredStatistic> statistics = Maps.newHashMap();

  private Map<String, Supplier<StatisticBinningStrategy>> binningStrategies = Maps.newHashMap();

  private StatisticsRegistry() {
    final ServiceLoader<StatisticsRegistrySPI> serviceLoader =
        ServiceLoader.load(StatisticsRegistrySPI.class);
    for (final StatisticsRegistrySPI providedStats : serviceLoader) {
      Arrays.stream(providedStats.getProvidedStatistics()).forEach(this::putStat);
      Arrays.stream(providedStats.getProvidedBinningStrategies()).forEach(this::putBinningStrategy);
    }
  }

  private void putStat(RegisteredStatistic stat) {
    final String key = stat.getStatisticsType().getString().toLowerCase();
    if (statistics.containsKey(key)) {
      LOGGER.warn(
          "Multiple statistics with the same type were found on the classpath. Only the first instance will be loaded!");
      return;
    }
    statistics.put(key, stat);
  }

  private void putBinningStrategy(RegisteredBinningStrategy strategy) {
    final String key = strategy.getStrategyName().toLowerCase();
    if (binningStrategies.containsKey(key)) {
      LOGGER.warn(
          "Multiple binning strategies with the same name were found on the classpath. Only the first instance will be loaded!");
      return;
    }
    binningStrategies.put(key, strategy.getConstructor());
  }


  public static StatisticsRegistry instance() {
    if (INSTANCE == null) {
      INSTANCE = new StatisticsRegistry();
    }
    return INSTANCE;
  }

  public Map<String, RegisteredStatistic> getStatistics() {
    return statistics;
  }

  /**
   * Retrieves the options class for the given statistics type.
   * 
   * @param statType the statistics type
   * @return the function that matches the given name, or {@code null} if it could not be found
   */
  public Statistic<StatisticValue<Object>> getStatisticsOptions(final StatisticType<?> statType) {
    return getStatisticsOptions(statType.getString());
  }

  /**
   * Retrieves the options class for the given statistics type.
   * 
   * @param statType the statistics type
   * @return the function that matches the given name, or {@code null} if it could not be found
   */
  public Statistic<StatisticValue<Object>> getStatisticsOptions(final String statType) {
    RegisteredStatistic statistic = statistics.get(statType.toLowerCase());
    if (statistic == null) {
      return null;
    }
    return statistic.getOptionsConstructor().get();
  }


  public StatisticType<StatisticValue<Object>> getStatisticType(final String statType) {
    RegisteredStatistic statistic = statistics.get(statType.toLowerCase());
    if (statistic == null) {
      return null;
    }
    return statistic.getStatisticsType();
  }

  public StatisticBinningStrategy getBinningStrategy(final String binningStrategyType) {
    Supplier<StatisticBinningStrategy> strategy =
        binningStrategies.get(binningStrategyType.toLowerCase());
    if (strategy == null) {
      return null;
    }
    return strategy.get();
  }

}
