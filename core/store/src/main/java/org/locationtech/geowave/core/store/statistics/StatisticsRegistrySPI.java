package org.locationtech.geowave.core.store.statistics;

import java.util.function.Supplier;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticBinningStrategy;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.adapter.AdapterStatistic;
import org.locationtech.geowave.core.store.statistics.field.FieldStatistic;
import org.locationtech.geowave.core.store.statistics.index.IndexStatistic;

public interface StatisticsRegistrySPI {

  public RegisteredStatistic[] getProvidedStatistics();

  public RegisteredBinningStrategy[] getProvidedBinningStrategies();

  /**
   * Associates a {@link StatisticType} with its options.
   */
  public static class RegisteredStatistic {
    private final StatisticType<StatisticValue<Object>> statType;
    private final Supplier<Statistic<StatisticValue<Object>>> optionsConstructor;

    private Statistic<?> prototype = null;

    /**
     * @param statType the statistics type
     * @param optionsConstructor the options constructor
     */
    @SuppressWarnings("unchecked")
    public RegisteredStatistic(
        final StatisticType<? extends StatisticValue<?>> statType,
        final Supplier<? extends Statistic<? extends StatisticValue<?>>> optionsConstructor) {
      this.statType = (StatisticType<StatisticValue<Object>>) statType;
      this.optionsConstructor = (Supplier<Statistic<StatisticValue<Object>>>) optionsConstructor;
    }

    /**
     * @return the statistics type
     */
    public StatisticType<StatisticValue<Object>> getStatisticsType() {
      return statType;
    }

    /**
     * @return the options constructor
     */
    public Supplier<Statistic<StatisticValue<Object>>> getOptionsConstructor() {
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

  public static class RegisteredBinningStrategy {
    private final String strategyName;
    private final Supplier<StatisticBinningStrategy> constructor;

    public RegisteredBinningStrategy(
        final String strategyName,
        Supplier<StatisticBinningStrategy> constructor) {
      this.strategyName = strategyName;
      this.constructor = constructor;
    }

    public String getStrategyName() {
      return strategyName;
    }

    public Supplier<StatisticBinningStrategy> getConstructor() {
      return constructor;
    }
  }

}
