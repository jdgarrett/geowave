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
import org.locationtech.geowave.core.cli.api.Command;
import org.locationtech.geowave.core.cli.api.OperationParams;
import org.locationtech.geowave.core.cli.exceptions.TargetNotFoundException;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.CloseableIterator;
import org.locationtech.geowave.core.store.adapter.InternalAdapterStore;
import org.locationtech.geowave.core.store.adapter.InternalDataAdapter;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.cli.store.DataStorePluginOptions;
import org.locationtech.geowave.core.store.index.IndexStore;
import org.locationtech.geowave.core.store.statistics.AdapterBinningStrategy;
import org.locationtech.geowave.core.store.statistics.DataStatisticsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

@GeowaveOperation(name = "list", parentOperation = StatsSection.class)
@Parameters(commandDescription = "Print statistics of a data store to standard output")
public class ListStatsCommand extends AbstractStatsCommand<String> implements Command {

  private static final Logger LOGGER = LoggerFactory.getLogger(ListStatsCommand.class);

  @Parameter(description = "<store name>")
  private List<String> parameters = new ArrayList<>();

  private String retValue = "";

  @Override
  public void execute(final OperationParams params) throws TargetNotFoundException {
    computeResults(params);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected boolean performStatsCommand(
      final DataStorePluginOptions storeOptions,
      final StatsCommandLineOptions statsOptions) throws IOException {

    final DataStatisticsStore statsStore = storeOptions.createDataStatisticsStore();
    final IndexStore indexStore = storeOptions.createIndexStore();

    final String[] authorizations = getAuthorizations(statsOptions.getAuthorizations());

    final StringBuilder builder = new StringBuilder();
    if (statsOptions.getIndexName() != null) {
      Index index = indexStore.getIndex(statsOptions.getIndexName());
      if (index == null) {
        throw new ParameterException(
            "An index called " + statsOptions.getIndexName() + " was not found");
      }
      try (CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> stats =
          statsStore.getIndexStatistics(index, null, statsOptions.getName())) {
        if (statsOptions.getTypeName() != null) {
          while (stats.hasNext()) {
            Statistic<StatisticValue<Object>> next =
                (Statistic<StatisticValue<Object>>) stats.next();
            if (next.getBinningStrategy() != null
                && next.getBinningStrategy() instanceof AdapterBinningStrategy) {
              StatisticValue<Object> value =
                  statsStore.getStatisticValue(
                      next,
                      new ByteArray(statsOptions.getTypeName()),
                      authorizations);
              // List it...
            }
          }
        } else {
          try (CloseableIterator<? extends StatisticValue<?>> statValues =
              statsStore.getStatisticValues(stats, null, authorizations)) {
            // STATS_TODO: List these
          }
        }
      }
    } else if (statsOptions.getTypeName() != null) {
      if (statsOptions.getFieldName() != null) {
        // List all field statistics
      } else {
        // Get all indices used by this adapter, list the bin of any index statistics binned by
        // adapter
        // Get all adapter statistics for it
        // Get all field statistics for it
      }
    } else if (statsOptions.getFieldName() != null) {
      throw new ParameterException("A type name must be supplied with a field name.");
    } else {
      // List all index, adapter, and field statistics that match the name (if supplied)
    }

    // STATS_TODO: This used to support JSON output, is that still needed?

    // try (CloseableIterator<DataStatistics<?, ?, ?>> statsIt =
    // statsStore.getAllDataStatistics(authorizations)) {
    // if (statsOptions.getJsonFormatFlag()) {
    // final JSONArray resultsArray = new JSONArray();
    // final JSONObject outputObject = new JSONObject();
    //
    // try {
    // // Output as JSON formatted strings
    // outputObject.put("dataType", adapter.getTypeName());
    // while (statsIt.hasNext()) {
    // final DataStatistics<?, ?, ?> stats = statsIt.next();
    // if (stats.getAdapterId() != adapter.getAdapterId()) {
    // continue;
    // }
    // resultsArray.add(stats.toJSONObject(internalAdapterStore));
    // }
    // outputObject.put("stats", resultsArray);
    // builder.append(outputObject.toString());
    // } catch (final JSONException ex) {
    // LOGGER.error("Unable to output statistic as JSON. ", ex);
    // }
    // }
    // // Output as strings
    // else {
    // while (statsIt.hasNext()) {
    // final DataStatistics<?, ?, ?> stats = statsIt.next();
    // if (stats.getAdapterId() != adapter.getAdapterId()) {
    // continue;
    // }
    // builder.append("[");
    // builder.append(String.format("%1$-20s", stats.getType().getString()));
    // builder.append("] ");
    // builder.append(stats.toString());
    // builder.append("\n");
    // }
    // }
    retValue = builder.toString().trim();
    JCommander.getConsole().println(retValue);

    return true;
  }

  public List<String> getParameters() {
    return parameters;
  }

  public void setParameters(final String storeName, final String adapterName) {
    parameters = new ArrayList<>();
    parameters.add(storeName);
    if (adapterName != null) {
      parameters.add(adapterName);
    }
  }

  @Override
  public String computeResults(final OperationParams params) throws TargetNotFoundException {
    // Ensure we have all the required arguments
    if (parameters.size() < 1) {
      throw new ParameterException("Requires arguments: <store name>");
    }
    super.run(params, parameters);
    if (!retValue.equals("")) {
      return retValue;
    } else {
      return "No Data Found";
    }
  }
}
