/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.adapter.vector.stats;

import java.io.IOException;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.locationtech.geowave.core.store.statistics.StatisticsIngestCallback;
import org.locationtech.geowave.core.store.statistics.field.FieldStatistic;
import org.locationtech.geowave.core.store.statistics.field.FieldStatisticType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beust.jcommander.Parameter;
import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.clearspring.analytics.stream.cardinality.HyperLogLogPlus;

/**
 * Hyperloglog provides an estimated cardinality of the number of unique values for an attribute.
 */
public class HyperLogLogStatistic extends
    FieldStatistic<HyperLogLogStatistic.HyperLogLogPlusValue> {
  private static final Logger LOGGER = LoggerFactory.getLogger(HyperLogLogStatistic.class);
  public static final FieldStatisticType<HyperLogLogPlusValue> STATS_TYPE =
      new FieldStatisticType<>("HYPER_LOG_LOG");

  // STATS_TODO: Should there be some kind of validation function to make sure inputs are valid?
  @Parameter(
      names = "--precision",
      description = "Number of bits per count value. 2^precision will be the maximum count per distinct value. Maximum precision is 32.")
  private int precision = 16;


  public HyperLogLogStatistic() {
    super(STATS_TYPE);
  }

  public HyperLogLogStatistic(final String typeName, final String fieldName) {
    super(STATS_TYPE, typeName, fieldName);
  }

  public HyperLogLogStatistic(final String typeName, final String fieldName, final int precision) {
    super(STATS_TYPE, typeName, fieldName);
    this.precision = precision;
  }

  @Override
  public String getDescription() {
    return "Provides an estimated cardinality of the number of unqiue values for an attribute.";
  }

  @Override
  public HyperLogLogPlusValue createEmpty() {
    return new HyperLogLogPlusValue(this, getFieldName(), precision);
  }

  @Override
  public boolean isCompatibleWith(Class<?> fieldClass) {
    return true;
  }

  public static class HyperLogLogPlusValue extends StatisticValue<HyperLogLogPlus> implements
      StatisticsIngestCallback {
    private HyperLogLogPlus loglog;
    private final String fieldName;

    public HyperLogLogPlusValue(
        final Statistic<?> statistic,
        final String fieldName,
        final int precision) {
      super(statistic);
      this.fieldName = fieldName;
      loglog = new HyperLogLogPlus(precision);
    }

    public long cardinality() {
      return loglog.cardinality();
    }

    @Override
    public void merge(StatisticValue<HyperLogLogPlus> merge) {
      if (merge instanceof HyperLogLogPlusValue) {
        try {
          loglog = (HyperLogLogPlus) ((HyperLogLogPlusValue) merge).loglog.merge(loglog);
        } catch (final CardinalityMergeException e) {
          throw new RuntimeException("Unable to merge counters", e);
        }
      }
    }

    @Override
    public <T> void entryIngested(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows) {
      final Object o = adapter.getFieldValue(entry, fieldName);
      if (o == null) {
        return;
      }
      loglog.offer(o.toString());
    }

    @Override
    public HyperLogLogPlus getValue() {
      return loglog;
    }

    @Override
    public byte[] toBinary() {
      try {
        return loglog.getBytes();
      } catch (final IOException e) {
        LOGGER.error("Exception while writing statistic", e);
      }
      return new byte[0];
    }

    @Override
    public void fromBinary(byte[] bytes) {
      try {
        loglog = HyperLogLogPlus.Builder.build(bytes);
      } catch (final IOException e) {
        LOGGER.error("Exception while reading statistic", e);
      }
    }
  }

  @Override
  public String toString() {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("hyperloglog[type=").append(getTypeName());
    buffer.append(", field=").append(getFieldName());
    buffer.append(", precision=").append(precision);
    buffer.append("]");
    return buffer.toString();
  }
}
