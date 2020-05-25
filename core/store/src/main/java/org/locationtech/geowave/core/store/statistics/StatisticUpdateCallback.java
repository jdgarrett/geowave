package org.locationtech.geowave.core.store.statistics;

import java.io.Closeable;
import java.io.Flushable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.callback.DeleteCallback;
import org.locationtech.geowave.core.store.callback.IngestCallback;
import org.locationtech.geowave.core.store.callback.ScanCallback;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Maps;

public class StatisticUpdateCallback<T> implements
    IngestCallback<T>,
    DeleteCallback<T, GeoWaveRow>,
    ScanCallback<T, GeoWaveRow>,
    AutoCloseable,
    Closeable,
    Flushable {

  private static final Logger LOGGER = LoggerFactory.getLogger(StatisticUpdateCallback.class);
  private static final int FLUSH_STATS_THRESHOLD = 1000000;

  private final Map<byte[], StatisticUpdateHandler<T, ?>> statisticValues = Maps.newHashMap();
  private final Object MUTEX = new Object();
  private final DataStatisticsStore statisticsStore;
  private final boolean skipFlush;
  private boolean overwrite;

  private int updateCount = 0;



  public StatisticUpdateCallback(
      final List<Statistic<?>> statistics,
      final DataStatisticsStore statisticsStore,
      final Index index,
      final DataTypeAdapter<T> adapter) {
    this.statisticsStore = statisticsStore;
    statistics.forEach(
        s -> statisticValues.put(s.getUniqueId(), new StatisticUpdateHandler<>(s, index, adapter)));
    // STATS_TODO: Is this system property even needed?
    final Object v = System.getProperty("StatsCompositionTool.skipFlush");
    skipFlush = ((v != null) && v.toString().equalsIgnoreCase("true"));
  }

  @Override
  public void entryDeleted(T entry, GeoWaveRow... rows) {
    synchronized (MUTEX) {
      for (StatisticUpdateHandler<T, ?> handler : statisticValues.values()) {
        handler.entryIngested(entry, rows);
      }
      updateCount++;
      checkStats();
    }
  }

  @Override
  public void entryIngested(T entry, GeoWaveRow... rows) {
    statisticValues.values().forEach(v -> v.entryIngested(entry, rows));
  }

  @Override
  public void entryScanned(T entry, GeoWaveRow row) {
    statisticValues.values().forEach(v -> v.entryScanned(entry, row));
  }

  private void checkStats() {
    if (!skipFlush && (updateCount >= FLUSH_STATS_THRESHOLD)) {
      updateCount = 0;
      flush();
    }
  }

  @Override
  public void flush() {
    synchronized (MUTEX) {
      for (final Entry<byte[], StatisticUpdateHandler<T, ?>> entry : statisticValues.entrySet()) {
        final Map<ByteArray, StatisticUpdater> statisticValues =
            entry.getValue().getStatisticValues();
        if (overwrite) {
          // STATS_TODO: Remove all statistic values for the given statistic.
          // statisticsStore.removeStatisticValues(entry.getKey());
        }
        for (final Entry<ByteArray, StatisticUpdater> visibilityStatistic : statisticValues.entrySet()) {
          // STATS_TODO: Write the value with the given visibility.
        }
        statisticValues.clear();
      }
      // just overwrite the initial set of values
      overwrite = false;
    }
  }

  @Override
  public void close() {
    flush();
  }

}
