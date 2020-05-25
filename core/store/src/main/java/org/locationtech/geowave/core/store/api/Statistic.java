package org.locationtech.geowave.core.store.api;

import org.locationtech.geowave.core.index.persist.Persistable;
import org.locationtech.geowave.core.store.EntryVisibilityHandler;
import org.locationtech.geowave.core.store.index.CommonIndexModel;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.StatisticUpdater;

public interface Statistic<R extends StatisticValue<?>> extends Persistable {

  public static String UNIQUE_ID_SEPARATOR = "|";

  public String getName();

  public StatisticType getStatisticType();

  public String getDescription();

  public R createEmpty();

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
  public byte[] getUniqueId();

  /**
   * 
   * @return
   */
  StatisticBinningStrategy getBinningStrategy();
}
