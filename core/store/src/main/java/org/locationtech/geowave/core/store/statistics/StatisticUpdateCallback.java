package org.locationtech.geowave.core.store.statistics;

import java.io.Closeable;
import java.io.Flushable;
import java.util.List;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.callback.DeleteCallback;
import org.locationtech.geowave.core.store.callback.IngestCallback;
import org.locationtech.geowave.core.store.callback.ScanCallback;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;

public class StatisticUpdateCallback<T> implements
    IngestCallback<T>,
    DeleteCallback<T, GeoWaveRow>,
    ScanCallback<T, GeoWaveRow>,
    AutoCloseable,
    Closeable,
    Flushable {

  private static final Logger LOGGER = LoggerFactory.getLogger(StatisticUpdateCallback.class);
  private static final int FLUSH_STATS_THRESHOLD = 1000000;

  private final List<StatisticUpdateHandler<T, ?, ?>> statisticUpdateHandlers;
  private final Object MUTEX = new Object();
  private final DataStatisticsStore statisticsStore;
  private final boolean skipFlush;
  private boolean overwrite;

  private int updateCount = 0;



  @SuppressWarnings({"rawtypes", "unchecked"})
  public StatisticUpdateCallback(
      final List<Statistic<? extends StatisticValue<?>>> statistics,
      final DataStatisticsStore statisticsStore,
      final Index index,
      final DataTypeAdapter<T> adapter) {
    this.statisticsStore = statisticsStore;
    statisticUpdateHandlers = Lists.newArrayListWithCapacity(statistics.size());
    for (Statistic<?> statistic : statistics) {
      StatisticUpdateHandler handler = new StatisticUpdateHandler(statistic, index, adapter);
      statisticUpdateHandlers.add(handler);
    }

    final Object v = System.getProperty("StatsCompositionTool.skipFlush");
    skipFlush = ((v != null) && v.toString().equalsIgnoreCase("true"));
  }

  @Override
  public void entryDeleted(T entry, GeoWaveRow... rows) {
    synchronized (MUTEX) {
      for (StatisticUpdateHandler<T, ?, ?> handler : statisticUpdateHandlers) {
        handler.entryDeleted(entry, rows);
      }
      updateCount++;
      checkStats();
    }
  }

  @Override
  public void entryIngested(T entry, GeoWaveRow... rows) {
    statisticUpdateHandlers.forEach(v -> v.entryIngested(entry, rows));
  }

  @Override
  public void entryScanned(T entry, GeoWaveRow row) {
    statisticUpdateHandlers.forEach(v -> v.entryScanned(entry, row));
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
      for (final StatisticUpdateHandler<T, ?, ?> updateHandler : statisticUpdateHandlers) {
        updateHandler.writeStatistics(statisticsStore, overwrite);
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
