/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.query.aggregate;

import org.locationtech.geowave.core.store.api.Aggregation;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticsIngestCallback;

// STATS_TODO: This is a cool idea, but this is a worthless class as it's currently implemented, because it doesn't take binning strategy/visibility into account
public class DataStatisticsAggregation<T> implements
    Aggregation<Statistic<?>, StatisticValue<?>, T> {
  private Statistic<?> statisticsParam;

  private StatisticValue<?> statisticsResult;

  public DataStatisticsAggregation() {}

  public DataStatisticsAggregation(final Statistic<?> statistics) {
    this.statisticsResult = statistics.createEmpty();
    this.statisticsParam = statistics;
  }

  @Override
  public void aggregate(final T entry) {
    if (statisticsResult instanceof StatisticsIngestCallback) {
      ((StatisticsIngestCallback) statisticsResult).entryIngested(null, entry);
    }
  }

  @Override
  public Statistic<?> getParameters() {
    return statisticsParam;
  }

  @Override
  public void setParameters(final Statistic<?> parameters) {
    this.statisticsParam = parameters;
  }

  @Override
  public void clearResult() {
    this.statisticsResult = statisticsParam.createEmpty();
  }

  @Override
  public StatisticValue<?> getResult() {
    return statisticsResult;
  }

  @Override
  public byte[] toBinary() {
    return new byte[] {};
  }

  @Override
  public void fromBinary(final byte[] bytes) {}

  @Override
  public byte[] resultToBinary(final StatisticValue<?> result) {
    return result.toBinary();
  }

  @Override
  public StatisticValue<?> resultFromBinary(final byte[] binary) {
    StatisticValue<?> value = statisticsParam.createEmpty();
    value.fromBinary(binary);
    return value;
  }
}
