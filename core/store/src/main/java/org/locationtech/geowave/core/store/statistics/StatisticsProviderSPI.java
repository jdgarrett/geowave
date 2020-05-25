package org.locationtech.geowave.core.store.statistics;

import java.util.function.Supplier;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.statistics.adapter.AdapterStatistic;
import org.locationtech.geowave.core.store.statistics.field.FieldStatistic;
import org.locationtech.geowave.core.store.statistics.index.IndexStatistic;

public interface StatisticsProviderSPI {

  /**
   * Associates a {@link StatisticType} with its options.
   */
  public static class ProvidedStatistic {
    private final StatisticType statType;
    private final Supplier<Statistic<?>> optionsConstructor;

    private Statistic<?> prototype = null;

    /**
     * @param statType the statistics type
     * @param optionsConstructor the options constructor
     */
    public ProvidedStatistic(
        final StatisticType statType,
        final Supplier<Statistic<?>> optionsConstructor) {
      this.statType = statType;
      this.optionsConstructor = optionsConstructor;
    }

    /**
     * @return the statistics type
     */
    public StatisticType getStatisticsType() {
      return statType;
    }

    /**
     * @return the options constructor
     */
    public Supplier<Statistic<?>> getOptionsConstructor() {
      return optionsConstructor;
    }

    // STATS_TODO: Is there a better way to handle this without overcomplicating the provider? Maybe
    // a static compatibility function? Maybe separate provided statistics by type?
    public boolean isAdapterStatistic() {
      return isAssignableTo(AdapterStatistic.class);
    }

    public boolean isIndexStatistic() {
      return isAssignableTo(IndexStatistic.class);
    }

    public boolean isFieldStatistic() {
      return isAssignableTo(FieldStatistic.class);
    }

    /**
     * @return the statistic class
     */
    private boolean isAssignableTo(final Class<?> clazz) {
      if (prototype == null) {
        prototype = optionsConstructor.get();
      }
      return clazz.isAssignableFrom(prototype.getClass());
    }

    public boolean isCompatibleWith(final Class<?> clazz) {
      if (prototype == null) {
        prototype = optionsConstructor.get();
      }
      return prototype.isCompatibleWith(clazz);
    }
  }

  public ProvidedStatistic[] getProvidedStatistics();

}
