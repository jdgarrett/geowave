package org.locationtech.geowave.core.store.api;

import org.locationtech.geowave.core.index.persist.Persistable;
import org.locationtech.geowave.core.store.EntryVisibilityHandler;
import org.locationtech.geowave.core.store.index.CommonIndexModel;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import org.locationtech.geowave.core.store.statistics.StatisticType;

/**
 * This is the base interface for all statistics in GeoWave.
 *
 * @param <V> the statistic value type
 */
public interface Statistic<V extends StatisticValue<?>> extends Persistable {

  /**
   * Statistics that are used by internal GeoWave systems use this tag.
   */
  public static final String INTERNAL_TAG = "internal";

  /**
   * Get the statistic type associated with the statistic.
   * 
   * @return the statistic type
   */
  public StatisticType<V> getStatisticType();

  /**
   * Get the tag for the statistic.
   * 
   * @return the tag
   */
  public String getTag();

  /**
   * Get a human-readable description of this statistic.
   * 
   * @return a description of the statistic
   */
  public String getDescription();

  /**
   * Create a new value for this statistic, initialized to a base state (no entries ingested).
   * 
   * @return the new value
   */
  public V createEmpty();

  /**
   * @return {@code true} if the statistic is an internal statistic
   */
  public default boolean isInternal() {
    return INTERNAL_TAG.equals(getTag());
  }

  /**
   * Get the visibility handler for the statistic.
   * 
   * @param indexModel the index model
   * @param type the data tyep
   * @return the visiblity handler
   */
  public <T> EntryVisibilityHandler<T> getVisibilityHandler(
      CommonIndexModel indexModel,
      DataTypeAdapter<T> type);

  /**
   * Determine if the statistic is compatible with the given class.
   * 
   * @param clazz the class to check
   * @return {@code true} if the statistic is compatble
   */
  public boolean isCompatibleWith(Class<?> clazz);

  /**
   * Return the unique identifier for the statistic.
   * 
   * @return the statistic id
   */
  public StatisticId<V> getId();

  /**
   * Returns the binning strategy used by the statistic.
   * 
   * @return the binning strategy, or {@code null} if there is none
   */
  StatisticBinningStrategy getBinningStrategy();
}
