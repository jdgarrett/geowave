package org.locationtech.geowave.core.store.statistics.index;

import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticType;

public class IndexStatisticType<V extends StatisticValue<?>> extends StatisticType<V> {
  private static final long serialVersionUID = 1L;

  public IndexStatisticType(final String id) {
    super(id);
  }
}
