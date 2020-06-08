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
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
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
   * Get registered index statistics.
   * 
   * @return a list of index statistics
   */
  public List<? extends Statistic<? extends StatisticValue<?>>> getRegisteredIndexStatistics() {
    return statistics.values().stream().filter(RegisteredStatistic::isIndexStatistic).map(
        s -> s.getStatisticConstructor().get()).collect(Collectors.toList());
  }

  /**
   * Get registered adapter statistics that are compatible with the the provided type.
   * 
   * @param type the type to get compatible statistics for
   * @return a list of compatible statistics
   */
  public List<? extends Statistic<? extends StatisticValue<?>>> getRegisteredAdapterStatistics(
      Class<?> adapterDataClass) {
    return statistics.values().stream().filter(
        s -> s.isAdapterStatistic() && s.isCompatibleWith(adapterDataClass)).map(
            s -> s.getStatisticConstructor().get()).collect(Collectors.toList());
  }

  /**
   * Get registered field statistics that are compatible with the the provided type.
   * 
   * @param type the type to get compatible statistics for
   * @param fieldName the field to get compatible statistics for
   * @return a map of compatible statistics, keyed by field name
   */
  public Map<String, List<? extends Statistic<? extends StatisticValue<?>>>> getRegisteredFieldStatistics(
      DataTypeAdapter<?> type,
      String fieldName) {
    Map<String, List<? extends Statistic<? extends StatisticValue<?>>>> fieldStatistics =
        Maps.newHashMap();
    final int fieldCount = type.getFieldCount();
    for (int i = 0; i < fieldCount; i++) {
      String name = type.getFieldName(i);
      Class<?> fieldClass = type.getFieldClass(i);
      if (fieldName == null || fieldName.equals(name)) {
        List<Statistic<StatisticValue<Object>>> fieldOptions =
            statistics.values().stream().filter(
                s -> s.isFieldStatistic() && s.isCompatibleWith(fieldClass)).map(
                    s -> s.getStatisticConstructor().get()).collect(Collectors.toList());
        fieldStatistics.put(name, fieldOptions);
      }
    }
    return fieldStatistics;
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
    return statistic.getStatisticConstructor().get();
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
