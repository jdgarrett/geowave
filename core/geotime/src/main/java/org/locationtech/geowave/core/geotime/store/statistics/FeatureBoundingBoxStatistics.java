/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.geotime.store.statistics;

import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.field.FieldStatistic;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

public class FeatureBoundingBoxStatistics extends
    FieldStatistic<FeatureBoundingBoxStatistics.FeatureBoundingBoxValue> {
  public static final StatisticType<FeatureBoundingBoxValue> STATS_TYPE = new StatisticType<>("BOUNDING_BOX");

  public FeatureBoundingBoxStatistics() {
    super(STATS_TYPE);
  }

  public FeatureBoundingBoxStatistics(final String typeName, final String fieldName) {
    super(STATS_TYPE, typeName, fieldName);
  }
  
  @Override
  public boolean isCompatibleWith(final Class<?> fieldClass) {
    return Geometry.class.isAssignableFrom(fieldClass);
  }
  
  @Override
  public String getDescription() {
    return "Maintains the bounding box for a geometry field.";
  }

  @Override
  public FeatureBoundingBoxValue createEmpty() {
    return new FeatureBoundingBoxValue(getFieldName());
  }
  
  public static class FeatureBoundingBoxValue extends BoundingBoxStatisticValue {
    private final String fieldName;
    
    public FeatureBoundingBoxValue(final String fieldName) {
      this.fieldName = fieldName;
    }

    @Override
    public <T> Envelope getEnvelope(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows) {
      Object fieldValue = adapter.getFieldValue(entry, fieldName);
      // STATS_TODO: Do we need to handle custom CRS here?  If so, we'll have to get it from the adapter through some interface...
//      if ((reprojectedType != null)
//          && (transform != null)
//          && !reprojectedType.getCoordinateReferenceSystem().equals(
//              entry.getType().getCoordinateReferenceSystem())) {
//        o =
//            GeometryUtils.crsTransform(entry, reprojectedType, transform).getAttribute(
//                getFieldName());
//      } else {
//        o = entry.getAttribute(getFieldName());
//      }
      if ((fieldValue != null) && (fieldValue instanceof Geometry)) {
        final Geometry geometry = (Geometry) fieldValue;
        if (!geometry.isEmpty()) {
          return geometry.getEnvelopeInternal();
        }
      }
      return null;
    }
    
  }
}
