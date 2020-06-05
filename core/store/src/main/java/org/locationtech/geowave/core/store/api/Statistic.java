package org.locationtech.geowave.core.store.api;

import org.locationtech.geowave.core.index.persist.Persistable;
import org.locationtech.geowave.core.store.EntryVisibilityHandler;
import org.locationtech.geowave.core.store.index.CommonIndexModel;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import org.locationtech.geowave.core.store.statistics.StatisticType;

public interface Statistic<V extends StatisticValue<?>> extends Persistable {

  public static final String INTERNAL_TAG = "internal";

  public String getTag();

  public StatisticType<V> getStatisticType();

  public String getDescription();

  public V createEmpty();

  public <T> EntryVisibilityHandler<T> getVisibilityHandler(
      CommonIndexModel indexModel,
      DataTypeAdapter<T> adapter);

  public boolean isCompatibleWith(Class<?> clazz);

  /**
   * Return an identifier to differentiate statistics of the same type. For example, the same
   * numeric statistic on multiple fields would include the field name in the unique identifier.
   * 
   * @return
   */
  public StatisticId<V> getId();

  /**
   * 
   * @return
   */
  StatisticBinningStrategy getBinningStrategy();
}
