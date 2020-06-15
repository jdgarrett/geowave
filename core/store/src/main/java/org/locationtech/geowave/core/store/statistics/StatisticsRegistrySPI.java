package org.locationtech.geowave.core.store.statistics;

import java.util.function.Supplier;
import org.locationtech.geowave.core.index.persist.Persistable;
import org.locationtech.geowave.core.index.persist.PersistableRegistrySpi;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticBinningStrategy;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.adapter.DataTypeStatisticType;
import org.locationtech.geowave.core.store.statistics.field.FieldStatisticType;
import org.locationtech.geowave.core.store.statistics.index.IndexStatisticType;

public abstract class StatisticsRegistrySPI implements PersistableRegistrySpi {

  @SuppressWarnings("unchecked")
  @Override
  public PersistableIdAndConstructor[] getSupportedPersistables() {
    RegisteredStatistic[] registeredStatistics = getProvidedStatistics();
    RegisteredBinningStrategy[] registeredBinningStrategies = getProvidedBinningStrategies();
    PersistableIdAndConstructor[] persistables =
        new PersistableIdAndConstructor[registeredStatistics.length * 2
            + registeredBinningStrategies.length];
    int persistableIndex = 0;
    for (RegisteredStatistic statistic : registeredStatistics) {
      persistables[persistableIndex++] =
          new PersistableIdAndConstructor(
              statistic.statisticPersistableId,
              (Supplier<Persistable>) (Supplier<?>) statistic.statisticConstructor);
      persistables[persistableIndex++] =
          new PersistableIdAndConstructor(
              statistic.valuePersistableId,
              (Supplier<Persistable>) (Supplier<?>) statistic.valueConstructor);
    }
    for (RegisteredBinningStrategy binningStrategy : registeredBinningStrategies) {
      persistables[persistableIndex++] =
          new PersistableIdAndConstructor(
              binningStrategy.persistableId,
              (Supplier<Persistable>) (Supplier<?>) binningStrategy.constructor);
    }
    return persistables;
  };

  public abstract RegisteredStatistic[] getProvidedStatistics();

  public abstract RegisteredBinningStrategy[] getProvidedBinningStrategies();

  /**
   * Associates a {@link StatisticType} with its options.
   */
  public static class RegisteredStatistic {
    private final StatisticType<StatisticValue<Object>> statType;
    private final Supplier<? extends Statistic<? extends StatisticValue<?>>> statisticConstructor;
    private final Supplier<? extends StatisticValue<?>> valueConstructor;
    private final short statisticPersistableId;
    private final short valuePersistableId;

    private Statistic<?> prototype = null;

    /**
     * @param statType the statistics type
     * @param optionsConstructor the options constructor
     */
    @SuppressWarnings("unchecked")
    public RegisteredStatistic(
        final StatisticType<? extends StatisticValue<?>> statType,
        final Supplier<? extends Statistic<? extends StatisticValue<?>>> statisticConstructor,
        final Supplier<? extends StatisticValue<?>> valueConstructor,
        final short statisticPersistableId,
        final short valuePersistableId) {
      this.statType = (StatisticType<StatisticValue<Object>>) statType;
      this.statisticConstructor = statisticConstructor;
      this.valueConstructor = valueConstructor;
      this.statisticPersistableId = statisticPersistableId;
      this.valuePersistableId = valuePersistableId;
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
    @SuppressWarnings("unchecked")
    public Supplier<Statistic<StatisticValue<Object>>> getStatisticConstructor() {
      return (Supplier<Statistic<StatisticValue<Object>>>) statisticConstructor;
    }

    public boolean isDataTypeStatistic() {
      return statType instanceof DataTypeStatisticType;
    }

    public boolean isIndexStatistic() {
      return statType instanceof IndexStatisticType;
    }

    public boolean isFieldStatistic() {
      return statType instanceof FieldStatisticType;
    }

    public boolean isCompatibleWith(final Class<?> clazz) {
      if (prototype == null) {
        prototype = statisticConstructor.get();
      }
      return prototype.isCompatibleWith(clazz);
    }
  }

  public static class RegisteredBinningStrategy {
    private final String strategyName;
    private final Supplier<? extends StatisticBinningStrategy> constructor;
    private final short persistableId;

    public RegisteredBinningStrategy(
        final String strategyName,
        final Supplier<? extends StatisticBinningStrategy> constructor,
        final short persistableId) {
      this.strategyName = strategyName;
      this.constructor = constructor;
      this.persistableId = persistableId;
    }

    public String getStrategyName() {
      return strategyName;
    }

    @SuppressWarnings("unchecked")
    public Supplier<StatisticBinningStrategy> getConstructor() {
      return (Supplier<StatisticBinningStrategy>) constructor;
    }
  }

}
