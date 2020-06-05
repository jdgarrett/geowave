package org.locationtech.geowave.adapter.vector.plugin.transaction;

import java.util.Map;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.StringUtils;
import org.locationtech.geowave.core.store.CloseableIterator;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.DataStatisticsStore;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import com.beust.jcommander.internal.Maps;
import com.google.common.primitives.Bytes;

public class StatisticsCache {

  protected final DataStatisticsStore statisticsStore;
  protected final DataTypeAdapter<?> adapter;
  protected final String[] authorizations;

  protected Map<ByteArray, StatisticValue<?>> cache = Maps.newHashMap();

  public StatisticsCache(
      final DataStatisticsStore statisticsStore,
      final DataTypeAdapter<?> adapter,
      String... authorizations) {
    this.statisticsStore = statisticsStore;
    this.adapter = adapter;
    this.authorizations = authorizations;
  }

  @SuppressWarnings("unchecked")
  public <V extends StatisticValue<R>, R> V getFieldStatistic(
      final StatisticType<V> statisticType,
      final String fieldName) {
    if (statisticType == null || fieldName == null) {
      return null;
    }
    ByteArray key =
        new ByteArray(
            Bytes.concat(
                statisticType.getBytes(),
                StatisticId.UNIQUE_ID_SEPARATOR,
                StringUtils.stringToBinary(fieldName)));
    if (cache.containsKey(key)) {
      return (V) cache.get(key);
    }
    V retVal = null;
    try (CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> statsIter =
        statisticsStore.getFieldStatistics(adapter, statisticType, fieldName, null)) {
      if (statsIter.hasNext()) {
        Statistic<V> stat = (Statistic<V>) statsIter.next();
        V value = statisticsStore.getStatisticValue(stat, authorizations);
        if (value != null) {
          retVal = value;
        }
      }
    }
    cache.put(key, retVal);
    return retVal;
  }

  @SuppressWarnings("unchecked")
  public <V extends StatisticValue<R>, R> V getAdapterStatistic(
      final StatisticType<V> statisticType) {
    ByteArray key = statisticType;
    if (cache.containsKey(key)) {
      return (V) cache.get(key);
    }
    V retVal = null;
    try (CloseableIterator<? extends Statistic<? extends StatisticValue<?>>> statsIter =
        statisticsStore.getAdapterStatistics(adapter, statisticType, null)) {
      if (statsIter.hasNext()) {
        Statistic<V> stat = (Statistic<V>) statsIter.next();
        V value = statisticsStore.getStatisticValue(stat, authorizations);
        if (value != null) {
          retVal = value;
        }
      }
    }
    cache.put(key, retVal);
    return retVal;
  }

}
