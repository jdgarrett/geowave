package org.locationtech.geowave.core.store.adapter.statistics;

public class DefaultStatisticsProvider implements StatisticsProviderSPI {
  public StatisticsTypeAndConstructor[] getProvidedStatistics() {
    return new StatisticsTypeAndConstructor[] {
        new StatisticsTypeAndConstructor(CountDataStatistics.STATS_TYPE, CountDataStatistics.Options::new)};
  }
}
