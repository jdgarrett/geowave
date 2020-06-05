package org.locationtech.geowave.core.geotime.store.statistics;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.locationtech.geowave.core.geotime.index.dimension.LatitudeDefinition;
import org.locationtech.geowave.core.geotime.index.dimension.LongitudeDefinition;
import org.locationtech.geowave.core.index.dimension.NumericDimensionDefinition;
import org.locationtech.geowave.core.index.sfc.data.NumericRange;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.locationtech.geowave.core.store.query.constraints.BasicQueryByClass.ConstraintData;
import org.locationtech.geowave.core.store.query.constraints.BasicQueryByClass.ConstraintSet;
import org.locationtech.geowave.core.store.statistics.StatisticsIngestCallback;
import org.locationtech.jts.geom.Envelope;

public abstract class AbstractBoundingBoxValue extends StatisticValue<Envelope> implements
    StatisticsIngestCallback {
  protected double minX = Double.MAX_VALUE;
  protected double minY = Double.MAX_VALUE;
  protected double maxX = -Double.MAX_VALUE;
  protected double maxY = -Double.MAX_VALUE;

  protected AbstractBoundingBoxValue(final Statistic<?> statistic) {
    super(statistic);
  }

  public boolean isSet() {
    if ((minX == Double.MAX_VALUE)
        || (minY == Double.MAX_VALUE)
        || (maxX == -Double.MAX_VALUE)
        || (maxY == -Double.MAX_VALUE)) {
      return false;
    }
    return true;
  }

  public double getMinX() {
    return minX;
  }

  public double getMinY() {
    return minY;
  }

  public double getMaxX() {
    return maxX;
  }

  public double getMaxY() {
    return maxY;
  }

  public double getWidth() {
    return maxX - minX;
  }

  public double getHeight() {
    return maxY - minY;
  }

  public ConstraintSet getConstraints() {
    // Create a NumericRange object using the x axis
    final NumericRange rangeLongitude = new NumericRange(minX, maxX);

    // Create a NumericRange object using the y axis
    final NumericRange rangeLatitude = new NumericRange(minY, maxY);

    final Map<Class<? extends NumericDimensionDefinition>, ConstraintData> constraintsPerDimension =
        new HashMap<>();
    // Create and return a new IndexRange array with an x and y axis
    // range
    constraintsPerDimension.put(
        LongitudeDefinition.class,
        new ConstraintData(rangeLongitude, true));
    constraintsPerDimension.put(LatitudeDefinition.class, new ConstraintData(rangeLatitude, true));
    return new ConstraintSet(constraintsPerDimension);
  }

  @Override
  public void merge(StatisticValue<Envelope> merge) {
    if ((merge != null) && (merge instanceof AbstractBoundingBoxValue)) {
      final AbstractBoundingBoxValue bboxStats = (AbstractBoundingBoxValue) merge;
      if (bboxStats.isSet()) {
        minX = Math.min(minX, bboxStats.minX);
        minY = Math.min(minY, bboxStats.minY);
        maxX = Math.max(maxX, bboxStats.maxX);
        maxY = Math.max(maxY, bboxStats.maxY);
      }
    }
  }

  @Override
  public <T> void entryIngested(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows) {
    final Envelope env = getEnvelope(adapter, entry);
    if (env != null) {
      minX = Math.min(minX, env.getMinX());
      minY = Math.min(minY, env.getMinY());
      maxX = Math.max(maxX, env.getMaxX());
      maxY = Math.max(maxY, env.getMaxY());
    }
  }

  public abstract <T> Envelope getEnvelope(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows);

  @Override
  public Envelope getValue() {
    if (isSet()) {
      return new Envelope(minX, maxX, minY, maxY);
    } else {
      return new Envelope();
    }
  }

  @Override
  public byte[] toBinary() {
    final ByteBuffer buffer = ByteBuffer.allocate(32);
    buffer.putDouble(minX);
    buffer.putDouble(minY);
    buffer.putDouble(maxX);
    buffer.putDouble(maxY);
    return buffer.array();
  }

  @Override
  public void fromBinary(byte[] bytes) {
    final ByteBuffer buffer = ByteBuffer.wrap(bytes);
    minX = buffer.getDouble();
    minY = buffer.getDouble();
    maxX = buffer.getDouble();
    maxY = buffer.getDouble();
  }

}
