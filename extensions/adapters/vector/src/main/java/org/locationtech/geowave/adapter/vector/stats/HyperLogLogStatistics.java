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
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.locationtech.geowave.core.geotime.store.statistics.FieldNameStatistic;
import org.locationtech.geowave.core.index.ByteArrayUtils;
import org.locationtech.geowave.core.index.Mergeable;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.store.adapter.statistics.AbstractDataStatistics;
import org.locationtech.geowave.core.store.adapter.statistics.FieldStatisticsQueryBuilder;
import org.locationtech.geowave.core.store.adapter.statistics.FieldStatisticsType;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.adapter.statistics.DataStatistics;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.StatisticsIngestCallback;
import org.locationtech.geowave.core.store.statistics.field.FieldStatistic;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beust.jcommander.Parameter;
import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.clearspring.analytics.stream.cardinality.HyperLogLogPlus;

/**
 * Hyperloglog provides an estimated cardinality of the number of unique values for an attribute.
 */
public class HyperLogLogStatistics extends
    FieldStatistic<HyperLogLogStatistics.HyperLogLogPlusValue> {
  private static final Logger LOGGER = LoggerFactory.getLogger(HyperLogLogStatistics.class);
  public static final StatisticType STATS_TYPE = new StatisticType("HYPER_LOG_LOG");
  
  // STATS_TODO: Should there be some kind of validation function to make sure inputs are valid?
  @Parameter(names = "--precision", description = "Number of bits per count value. 2^precision will be the maximum count per distinct value. Maximum precision is 32.")
  private int precision = 16;


  public HyperLogLogStatistics() {
    super(STATS_TYPE);
  }
  
  public HyperLogLogStatistics(final String typeName, final String fieldName) {
    super(STATS_TYPE, typeName, fieldName);
  }

  public HyperLogLogStatistics(final String typeName, final String fieldName, final int precision) {
    super(STATS_TYPE, typeName, fieldName);
    this.precision = precision;
  }
  
  @Override
  public String getDescription() {
    return "Provides an estimated cardinality of the number of unqiue values for an attribute.";
  }
  
  @Override
  public HyperLogLogPlusValue createEmpty() {
    return new HyperLogLogPlusValue(getFieldName(), precision);
  }
  
  @Override
  public boolean isCompatibleWith(Class<?> fieldClass) {
    return true;
  }
  
  public static class HyperLogLogPlusValue implements StatisticValue<HyperLogLogPlus>, StatisticsIngestCallback {
    private HyperLogLogPlus loglog;
    private final String fieldName;
    
    public HyperLogLogPlusValue(final String fieldName, final int precision) {
      this.fieldName = fieldName;
      loglog = new HyperLogLogPlus(precision);
    }
    
    public long cardinality() {
      return loglog.cardinality();
    }

    @Override
    public void merge(Mergeable merge) {
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
      // TODO Auto-generated method stub
      return null;
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
