/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.cli.stats;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.locationtech.geowave.core.cli.annotations.GeowaveOperation;
import org.locationtech.geowave.core.cli.api.OperationParams;
import org.locationtech.geowave.core.cli.api.ServiceEnabledCommand;
import org.locationtech.geowave.core.cli.utils.ConsolePrinter;
import org.locationtech.geowave.core.store.api.DataStore;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.cli.store.DataStorePluginOptions;
import org.locationtech.geowave.core.store.cli.store.StoreLoader;
import org.locationtech.geowave.core.store.statistics.DataStatisticsStore;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@GeowaveOperation(name = "listtypes", parentOperation = StatsSection.class)
@Parameters(
    commandDescription = "List statistics types that are compatible with the given data store")
public class ListStatTypesCommand extends ServiceEnabledCommand<Void> {

  @Parameter(description = "<store name>")
  private final List<String> parameters = new ArrayList<>();

  @Parameter(
      names = {"--typeName"},
      description = "If specified, only statistics that are compaitbile with this type will be listed.")
  private String typeName = null;

  @Parameter(
      names = {"--fieldName"},
      description = "If specified, only statistics that are compatible with this field will be displayed.  Requires a typeName to be specified.")
  private String fieldName = null;


  @Override
  public void execute(final OperationParams params) {
    computeResults(params);
  }

  @Override
  public Void computeResults(final OperationParams params) {
    if (parameters.size() != 1) {
      throw new ParameterException("Requires arguments: <store name>");
    }

    final String storeName = parameters.get(0);

    // Attempt to load store.
    final File configFile = getGeoWaveConfigFile(params);

    final StoreLoader inputStoreLoader = new StoreLoader(storeName);
    if (!inputStoreLoader.loadFromConfig(configFile)) {
      throw new ParameterException("Cannot find store name: " + storeName);
    }
    final DataStorePluginOptions storeOptions = inputStoreLoader.getDataStorePlugin();

    final DataStore dataStore = storeOptions.createDataStore();

    final DataStatisticsStore statsStore = storeOptions.createDataStatisticsStore();
    final DataTypeAdapter<?> adapter = typeName != null ? dataStore.getType(typeName) : null;
    if (typeName != null && adapter == null) {
      throw new ParameterException("Unrecognized type name: " + typeName);
    }

    List<? extends Statistic<? extends StatisticValue<?>>> indexStats = statsStore.getRegisteredIndexStatistics();

    ConsolePrinter printer = new ConsolePrinter(0, Integer.MAX_VALUE);
    Map<String, List<? extends Statistic<? extends StatisticValue<?>>>> adapterStats = Maps.newHashMap();
    Map<String, Map<String, List<? extends Statistic<? extends StatisticValue<?>>>>> fieldStats = Maps.newHashMap();
    if (adapter == null) {
      DataTypeAdapter<?>[] adapters = dataStore.getTypes();
      for (DataTypeAdapter<?> dataAdapter : adapters) {
        adapterStats.put(
            dataAdapter.getTypeName(),
            statsStore.getRegisteredAdapterStatistics(dataAdapter.getDataClass()));
        fieldStats.put(
            dataAdapter.getTypeName(),
            statsStore.getRegisteredFieldStatistics(dataAdapter, null));
      }

    } else {
      adapterStats.put(
          adapter.getTypeName(),
          statsStore.getRegisteredAdapterStatistics(adapter.getDataClass()));
      fieldStats.put(
          adapter.getTypeName(),
          statsStore.getRegisteredFieldStatistics(adapter, fieldName));
    }

    displayIndexStats(printer, indexStats);
    displayAdapterStats(printer, adapterStats);
    displayFieldStats(printer, fieldStats);
    return null;
  }

  private void displayIndexStats(ConsolePrinter printer, List<? extends Statistic<? extends StatisticValue<?>>> stats) {
    JCommander.getConsole().println("General index statistics: ");
    List<List<Object>> rows = Lists.newArrayListWithCapacity(stats.size());

    for (Statistic<?> o : stats) {
      rows.add(Arrays.asList(o.getStatisticType(), o.getDescription()));
    }
    printer.print(Arrays.asList("Statistic", "Description"), rows);
  }

  private void displayAdapterStats(ConsolePrinter printer, Map<String, List<? extends Statistic<? extends StatisticValue<?>>>> stats) {
    JCommander.getConsole().println("General data type statistics: ");
    List<List<Object>> rows = Lists.newArrayListWithCapacity(stats.size());
    for (Entry<String, List<? extends Statistic<? extends StatisticValue<?>>>> adapterStats : stats.entrySet()) {
      boolean first = true;
      for (Statistic<?> o : adapterStats.getValue()) {
        rows.add(
            Arrays.asList(
                first ? adapterStats.getKey() : "",
                o.getStatisticType(),
                o.getDescription()));
        first = false;
      }
    }
    printer.print(Arrays.asList("Type", "Statistic", "Description"), rows);
  }

  private void displayFieldStats(
      ConsolePrinter printer,
      Map<String, Map<String, List<? extends Statistic<? extends StatisticValue<?>>>>> stats) {
    JCommander.getConsole().println("Compatible field statistics: ");
    List<List<Object>> rows = Lists.newArrayListWithCapacity(stats.size());
    for (Entry<String, Map<String, List<? extends Statistic<? extends StatisticValue<?>>>>> adapterStats : stats.entrySet()) {
      boolean firstAdapter = true;
      for (Entry<String, List<? extends Statistic<? extends StatisticValue<?>>>> fieldStats : adapterStats.getValue().entrySet()) {
        boolean firstField = true;
        for (Statistic<?> o : fieldStats.getValue()) {
          rows.add(
              Arrays.asList(
                  firstAdapter ? adapterStats.getKey() : "",
                  firstField ? fieldStats.getKey() : "",
                  o.getStatisticType(),
                  o.getDescription()));
          firstAdapter = false;
          firstField = false;
        }
      }
    }
    printer.print(Arrays.asList("Type", "Field", "Statistic", "Description"), rows);
  }
}
