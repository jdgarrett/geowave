/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.statistics.binning;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Range;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.lexicoder.Lexicoders;
import org.locationtech.geowave.core.store.api.BinConstraints.ByteArrayConstraints;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.locationtech.geowave.core.store.statistics.query.BinConstraintsImpl.ExplicitConstraints;
import com.beust.jcommander.Parameter;

/**
 * Statistic binning strategy that bins statistic values by the numeric representation of the value
 * of a given field. By default it will truncate decimal places and will bin by the integer.
 * However, an "offset" and "interval" can be provided to bin numbers at any regular step-sized
 * increment from an origin value. A statistic using this binning strategy can be constrained using
 * numeric ranges (Apache-Commons `Range<? extends Number>` class can be used as a constraint).
 */
public class NumericRangeFieldValueBinningStrategy extends FieldValueBinningStrategy {
  public static final String NAME = "NUMERIC_FIELD_VALUE";
  @Parameter(
      names = "--binInterval",
      description = "Field that contains the bin interval.  Defaults to 1.")
  private double interval = 1;

  @Parameter(
      names = "--binOffset",
      description = "Offset the field values by a given amount.  Defaults to 0.")
  private double offset = 0;

  @Override
  public String getStrategyName() {
    return NAME;
  }

  @Override
  public String getDescription() {
    return "Bin the statistic by the numeric value of a specified field.";
  }

  @Override
  public Class<?>[] supportedConstraintClasses() {
    return ArrayUtils.addAll(
        super.supportedConstraintClasses(),
        Number.class,
        Range.class,
        Range[].class);
  }

  @Override
  public ByteArrayConstraints constraints(final Object constraint) {
    if (constraint instanceof Number) {
      return new ExplicitConstraints(new ByteArray[] {getNumericBin((Number) constraint)});
    } else if (constraint instanceof Range) {
      return new ExplicitConstraints(getNumericBins((Range<? extends Number>) constraint));
    } else if (constraint instanceof Range[]) {
      final Stream<ByteArray[]> stream =
          Arrays.stream((Range[]) constraint).map(this::getNumericBins);
      return new ExplicitConstraints(stream.flatMap(Arrays::stream).toArray(ByteArray[]::new));
    }
    return super.constraints(constraint);
  }

  @Override
  public <T> ByteArray[] getBins(
      final DataTypeAdapter<T> adapter,
      final T entry,
      final GeoWaveRow... rows) {
    return new ByteArray[] {getSafeNumericBin(adapter.getFieldValue(entry, fieldName))};
  }

  private ByteArray getSafeNumericBin(final Object value) {
    if ((value == null) || !(value instanceof Number)) {
      return new ByteArray();
    }
    return getNumericBin((Number) value);
  }

  private ByteArray getNumericBin(final Number value) {
    final long bin = (long) Math.floor(((value.doubleValue() + offset) / interval));
    return new ByteArray(Lexicoders.LONG.toByteArray(bin));
  }

  private ByteArray[] getNumericBins(final Range<? extends Number> value) {
    final long minBin = (long) Math.floor(((value.getMinimum().doubleValue() + offset) / interval));
    final long maxBin = (long) Math.floor(((value.getMaximum().doubleValue() + offset) / interval));
    return LongStream.rangeClosed(minBin, maxBin).mapToObj(Lexicoders.LONG::toByteArray).map(
        ByteArray::new).toArray(ByteArray[]::new);
  }

  @Override
  public byte[] toBinary() {
    final byte[] parentBinary = super.toBinary();
    final ByteBuffer buf = ByteBuffer.allocate(parentBinary.length + 16);
    buf.put(parentBinary);
    buf.putDouble(interval);
    buf.putDouble(offset);
    return buf.array();
  }

  @Override
  public void fromBinary(final byte[] bytes) {
    final ByteBuffer buf = ByteBuffer.wrap(bytes);
    final byte[] parentBinary = new byte[bytes.length - 16];
    buf.get(parentBinary);
    super.fromBinary(parentBinary);
    interval = buf.getDouble();
    offset = buf.getDouble();
  }

  private Range<Double> getRange(final ByteArray bin) {
    final double low = (Lexicoders.LONG.fromByteArray(bin.getBytes()) * interval) - offset;
    return Range.between(low, low + interval);
  }

  @Override
  public String binToString(final ByteArray bin) {
    return rangeToString(getRange(bin));
  }

  private static String rangeToString(final Range<Double> range) {
    final StringBuilder buf = new StringBuilder(32);
    buf.append('[');
    buf.append(range.getMinimum());
    buf.append("..");
    buf.append(range.getMaximum());
    buf.append(')');
    return buf.toString();
  }
}
