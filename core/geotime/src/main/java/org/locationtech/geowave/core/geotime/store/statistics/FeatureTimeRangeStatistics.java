/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.geotime.store.statistics;

import java.util.Calendar;
import java.util.Date;
import org.locationtech.geowave.core.geotime.util.TimeUtils;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.field.FieldStatistic;
import org.threeten.extra.Interval;

public class FeatureTimeRangeStatistics extends
    FieldStatistic<FeatureTimeRangeStatistics.FeatureTimeRangeValue> {
  public static final StatisticType<FeatureTimeRangeValue> STATS_TYPE = new StatisticType<>("TIME_RANGE");

  public FeatureTimeRangeStatistics() {
    super(STATS_TYPE);
  }

  public FeatureTimeRangeStatistics(final String typeName, final String fieldName) {
    super(STATS_TYPE, typeName, fieldName);
  }
  
  @Override
  public String getDescription() {
    return "Maintains the time range of a temporal field.";
  }

  @Override
  public FeatureTimeRangeValue createEmpty() {
    return new FeatureTimeRangeValue(getFieldName());
  }

  @Override
  public boolean isCompatibleWith(Class<?> fieldClass) {
    return Date.class.isAssignableFrom(fieldClass) || Calendar.class.isAssignableFrom(fieldClass) || Number.class.isAssignableFrom(fieldClass);
  }
  
  public static class FeatureTimeRangeValue extends TimeRangeStatisticValue {
    
    private final String fieldName;
    
    public FeatureTimeRangeValue(final String fieldName) {
      this.fieldName = fieldName;
    }

    @Override
    protected <T> Interval getInterval(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows) {
      Object fieldValue = adapter.getFieldValue(entry, fieldName);
      return TimeUtils.getInterval(fieldValue);
    }
    
  }
}
