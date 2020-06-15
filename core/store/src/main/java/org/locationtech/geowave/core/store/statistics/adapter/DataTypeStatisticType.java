package org.locationtech.geowave.core.store.statistics.adapter;

import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticType;

public class DataTypeStatisticType<V extends StatisticValue<?>> extends StatisticType<V> {
  private static final long serialVersionUID = 1L;

  public DataTypeStatisticType(final String id) {
    super(id);
  }
}
