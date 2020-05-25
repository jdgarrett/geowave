/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.statistics.field;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.locationtech.geowave.core.index.Mergeable;
import org.locationtech.geowave.core.store.adapter.statistics.histogram.FixedBinNumericHistogram;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.StatisticsIngestCallback;
import com.beust.jcommander.Parameter;

/**
 * Fixed number of bins for a histogram. Unless configured, the range will expand dynamically,
 * redistributing the data as necessary into the wider bins.
 *
 * <p> The advantage of constraining the range of the statistic is to ignore values outside the
 * range, such as erroneous values. Erroneous values force extremes in the histogram. For example,
 * if the expected range of values falls between 0 and 1 and a value of 10000 occurs, then a single
 * bin contains the entire population between 0 and 1, a single bin represents the single value of
 * 10000. If there are extremes in the data, then use `FeatureNumericHistogramStatistics` instead.
 *
 * <p> The default number of bins is 32.
 */
public class FixedBinNumericHistogramStatistic extends
    FieldStatistic<FixedBinNumericHistogramStatistic.FixedBinNumericHistogramValue> {

  public static final StatisticType STATS_TYPE = new StatisticType("FIXED_BIN_NUMERIC_HISTOGRAM");

  @Parameter(names = "--numBins", description = "The number of bins for the histogram.")
  private int numBins = 1024;

  @Parameter(
      names = "--minValue",
      description = "The minimum value for the histogram. If both min and max are not specified, the range will be unconstrained.")
  private Double minValue = null;

  @Parameter(
      names = "--maxValue",
      description = "The maximum value for the histogram. If both min and max are not specified, the range will be unconstrained.")
  private Double maxValue = null;

  public FixedBinNumericHistogramStatistic() {
    super(STATS_TYPE);
  }

  public FixedBinNumericHistogramStatistic(final String typeName, final String fieldName) {
    this(typeName, fieldName, 1024);
  }

  public FixedBinNumericHistogramStatistic(
      final String typeName,
      final String fieldName,
      final int bins) {
    super(STATS_TYPE, typeName, fieldName);
    this.numBins = bins;
  }

  public FixedBinNumericHistogramStatistic(
      final String typeName,
      final String fieldName,
      final int bins,
      final double minValue,
      final double maxValue) {
    super(STATS_TYPE, typeName, fieldName);
    this.numBins = bins;
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  @Override
  public boolean isCompatibleWith(Class<?> fieldClass) {
    return Number.class.isAssignableFrom(fieldClass) || Date.class.isAssignableFrom(fieldClass);
  }

  @Override
  public String getDescription() {
    return "A numeric histogram with a fixed number of bins.";
  }

  @Override
  public FixedBinNumericHistogramValue createEmpty() {
    if (minValue == null || maxValue == null) {
      return new FixedBinNumericHistogramValue(getFieldName(), numBins);
    }
    return new FixedBinNumericHistogramValue(getFieldName(), numBins, minValue, maxValue);
  }

  public static class FixedBinNumericHistogramValue implements
      StatisticValue<FixedBinNumericHistogram>,
      StatisticsIngestCallback {

    private final String fieldName;
    private FixedBinNumericHistogram histogram;

    private FixedBinNumericHistogramValue(String fieldName, int bins) {
      this.fieldName = fieldName;
      histogram = new FixedBinNumericHistogram(bins);
    }

    private FixedBinNumericHistogramValue(
        String fieldName,
        int bins,
        double minValue,
        double maxValue) {
      this.fieldName = fieldName;
      histogram = new FixedBinNumericHistogram(bins, minValue, maxValue);
    }

    @Override
    public void merge(Mergeable merge) {
      if (merge instanceof FixedBinNumericHistogramValue) {
        final FixedBinNumericHistogramValue tobeMerged = (FixedBinNumericHistogramValue) merge;
        histogram.merge(tobeMerged.histogram);
      }
    }

    @Override
    public <T> void entryIngested(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows) {
      final Object o = adapter.getFieldValue(entry, fieldName);
      if (o == null) {
        return;
      }
      double value;
      if (o instanceof Date) {
        value = ((Date) o).getTime();
      } else if (o instanceof Number) {
        value = ((Number) o).doubleValue();
      } else {
        return;
      }
      histogram.add(1, value);
    }

    @Override
    public FixedBinNumericHistogram getValue() {
      return histogram;
    }

    @Override
    public byte[] toBinary() {
      final ByteBuffer buffer = ByteBuffer.allocate(histogram.bufferSize());
      histogram.toBinary(buffer);
      return buffer.array();
    }

    @Override
    public void fromBinary(byte[] bytes) {
      histogram = new FixedBinNumericHistogram();
      histogram.fromBinary(ByteBuffer.wrap(bytes));
    }

    public double[] quantile(final int bins) {
      return histogram.quantile(bins);
    }

    public double cdf(final double val) {
      return histogram.cdf(val);
    }

    public double quantile(final double percentage) {
      return histogram.quantile(percentage);
    }

    public double percentPopulationOverRange(final double start, final double stop) {
      return cdf(stop) - cdf(start);
    }

    public long totalSampleSize() {
      return histogram.getTotalCount();
    }

    public long[] count(final int binSize) {
      return histogram.count(binSize);
    }
  }

  @Override
  public String toString() {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("histogram[type=").append(getTypeName());
    buffer.append(", field=").append(getFieldName());
    buffer.append("]");
    return buffer.toString();
  }

  // @Override
  // protected String resultsName() {
  // return "histogram";
  // }
  //
  // @Override
  // protected Object resultsValue() {
  // final Map<String, Object> value = new HashMap<>();
  //
  // value.put("range_min", histogram.getMinValue());
  // value.put("range_max", histogram.getMaxValue());
  // final Collection<Double> binsArray = new ArrayList<>();
  // for (final double v : this.quantile(10)) {
  // binsArray.add(v);
  // }
  // value.put("bins", binsArray);
  //
  // final Collection<Long> countsArray = new ArrayList<>();
  // for (final long v : count(10)) {
  // countsArray.add(v);
  // }
  // value.put("counts", countsArray);
  // return value;
  // }
}
