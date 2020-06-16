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
      Arrays.stream(providedStats.getRegisteredStatistics()).forEach(this::putStat);
      Arrays.stream(providedStats.getRegisteredBinningStrategies()).forEach(
          this::putBinningStrategy);
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

  /**
   * Get registered index statistics that are compatible with the given index class.
   * 
   * @param indexClass the class of the index
   * @return a list of index statistics
   */
  public List<? extends Statistic<? extends StatisticValue<?>>> getRegisteredIndexStatistics(
      Class<?> indexClass) {
    return statistics.values().stream().filter(
        s -> s.isIndexStatistic() && s.isCompatibleWith(indexClass)).map(
            s -> s.getStatisticConstructor().get()).collect(Collectors.toList());
  }

  /**
   * Get registered data type statistics that are compatible with the the data type class.
   * 
   * @param adapterDataClass the class of the entries of the data type adapter
   * @return a list of compatible statistics
   */
  public List<? extends Statistic<? extends StatisticValue<?>>> getRegisteredDataTypeStatistics(
      Class<?> adapterDataClass) {
    return statistics.values().stream().filter(
        s -> s.isDataTypeStatistic() && s.isCompatibleWith(adapterDataClass)).map(
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
   * Get all registered statistics.
   * 
   * @return a list of registered statistics
   */
  public List<? extends Statistic<? extends StatisticValue<?>>> getAllRegisteredStatistics() {
    return statistics.values().stream().map(s -> s.getStatisticConstructor().get()).collect(
        Collectors.toList());
  }

  /**
   * Get all registered binning strategies.
   * 
   * @return a list of registered binning strategies
   */
  public List<StatisticBinningStrategy> getAllRegisteredBinningStrategies() {
    return binningStrategies.values().stream().map(b -> b.get()).collect(Collectors.toList());
  }

  /**
   * Retrieves the statistic of the given statistic type.
   * 
   * @param statType the statistic type
   * @return the statistic that matches the given name, or {@code null} if it could not be found
   */
  public Statistic<StatisticValue<Object>> getStatistic(final StatisticType<?> statType) {
    return getStatistic(statType.getString());
  }

  /**
   * Retrieves the statistic of the given statistic type.
   * 
   * @param statType the statistic type
   * @return the statistic that matches the given name, or {@code null} if it could not be found
   */
  public Statistic<StatisticValue<Object>> getStatistic(final String statType) {
    RegisteredStatistic statistic = statistics.get(statType.toLowerCase());
    if (statistic == null) {
      return null;
    }
    return statistic.getStatisticConstructor().get();
  }


  /**
   * Retrieves the statistic type that matches the given string.
   * 
   * @param statType the statistic type to get
   * @return the statistic type, or {@code null} if a matching statistic type could not be found
   */
  public StatisticType<StatisticValue<Object>> getStatisticType(final String statType) {
    RegisteredStatistic statistic = statistics.get(statType.toLowerCase());
    if (statistic == null) {
      return null;
    }
    return statistic.getStatisticsType();
  }

  /**
   * Retrieves the binning strategy that matches the given string.
   * 
   * @param binningStrategyType the binning strategy to get
   * @return the binning strategy, or {@code null} if a matching binning strategy could not be found
   */
  public StatisticBinningStrategy getBinningStrategy(final String binningStrategyType) {
    Supplier<StatisticBinningStrategy> strategy =
        binningStrategies.get(binningStrategyType.toLowerCase());
    if (strategy == null) {
      return null;
    }
    return strategy.get();
  }

}
