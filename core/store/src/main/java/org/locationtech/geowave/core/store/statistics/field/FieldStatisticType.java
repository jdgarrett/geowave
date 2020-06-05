package org.locationtech.geowave.core.store.statistics.field;

import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticType;

public class FieldStatisticType<V extends StatisticValue<?>> extends StatisticType<V> {
  private static final long serialVersionUID = 1L;

  public FieldStatisticType(final String id) {
    super(id);
  }

}
