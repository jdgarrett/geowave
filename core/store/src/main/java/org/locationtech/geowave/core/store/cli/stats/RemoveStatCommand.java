/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.cli.stats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.geowave.core.cli.annotations.GeowaveOperation;
import org.locationtech.geowave.core.cli.api.OperationParams;
import org.locationtech.geowave.core.store.CloseableIterator;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.cli.store.DataStorePluginOptions;
import org.locationtech.geowave.core.store.index.IndexStore;
import org.locationtech.geowave.core.store.statistics.DataStatisticsStore;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.StatisticsRegistry;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.clearspring.analytics.util.Lists;

@GeowaveOperation(name = "rm", parentOperation = StatsSection.class)
@Parameters(commandDescription = "Remove a statistic from a data store")
public class RemoveStatCommand extends AbstractStatsCommand<Void> {

  @Parameter(description = "<store name> <stat type>")
  private final List<String> parameters = new ArrayList<>();
  
  @Parameter(names = "--all", description = "If specified, all matching statistics will be removed.")
  private boolean all = false;

  private String statType = null;

  @Override
  public void execute(final OperationParams params) {
    computeResults(params);
  }

  @Override
  protected boolean performStatsCommand(
      final DataStorePluginOptions storeOptions,
      final StatsCommandLineOptions statsOptions) throws IOException {

    // Remove the stat
    final DataStatisticsStore statStore = storeOptions.createDataStatisticsStore();
    
    StatisticType<StatisticValue<Object>> statisticType = StatisticsRegistry.instance().getStatisticType(statType);
    
    if (statisticType == null) {
      throw new ParameterException("Unrecognized statistic type: " + statType);
    }
    
    List<Statistic<? extends StatisticValue<?>>> toRemove = Lists.newArrayList();
    
    if (statsOptions.getIndexName() != null) {
      if (statsOptions.getTypeName() != null || statsOptions.getFieldName() != null) {
        throw new ParameterException(
            "Unable to supply both an index and a type/field when removing a statistic.  If removing an index statistic, supply an index name, otherwise, supply a type name.");
      }
      final IndexStore indexStore = storeOptions.createIndexStore();
      final Index index = indexStore.getIndex(statsOptions.getIndexName());
      if (index == null) {
        throw new ParameterException("Unable to find an index named: " + statsOptions.getIndexName());
      }
      try (CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> stats = statStore.getIndexStatistics(index, statisticType, statsOptions.getName())) {
        while(stats.hasNext()) {
          toRemove.add(stats.next());
        }
      }
    } else if (statsOptions.getTypeName() != null) {
      if (statsOptions.getFieldName() != null) {
        // remove field statistics that match the given name (if supplied)
      }
    }
    
    if (toRemove.isEmpty()) {
      throw new ParameterException("A matching statistic could not be found");
    }

    if (!all && toRemove.size() > 1) {
      throw new ParameterException("Multiple statistics matched the given parameters, if this is intentional, please supply the --all option.");
    }

    if (!statStore.removeStatistics(toRemove.iterator())) {
      throw new RuntimeException("Unable to remove statistic: " + statType);
    }

    return true;
  }

  @Override
  public Void computeResults(final OperationParams params) {
    // Ensure we have all the required arguments
    if (parameters.size() != 2) {
      throw new ParameterException("Requires arguments: <store name> <stat type>");
    }

    statType = parameters.get(1);

    super.run(params, parameters);
    return null;
  }
}
