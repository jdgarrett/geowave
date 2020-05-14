package org.locationtech.geowave.core.store.adapter.statistics;

import java.util.function.Supplier;
import org.locationtech.geowave.core.store.api.StatisticsOptions;

public interface StatisticsProviderSPI {
  
  /**
   * Associates a {@link StatisticsType} with its options.
   */
  public static class StatisticsTypeAndConstructor {
    private final StatisticsType statType;
    private final Supplier<StatisticsOptions> optionsConstructor;

    /**
     * @param statType the statistics type
     * @param optionsConstructor the options constructor
     */
    public StatisticsTypeAndConstructor(
        final StatisticsType statType,
        final Supplier<StatisticsOptions> optionsConstructor) {
      this.statType = statType;
      this.optionsConstructor = optionsConstructor;
    }

    /**
     * @return the statistics type
     */
    public StatisticsType getStatisticsType() {
      return statType;
    }

    /**
     * @return the options constructor
     */
    public Supplier<StatisticsOptions> getOptionsConstructor() {
      return optionsConstructor;
    }
  }
  
  public StatisticsTypeAndConstructor[] getProvidedStatistics();

}
