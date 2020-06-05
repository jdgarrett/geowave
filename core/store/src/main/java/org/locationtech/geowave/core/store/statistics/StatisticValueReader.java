package org.locationtech.geowave.core.store.statistics;

import java.util.Arrays;
import org.locationtech.geowave.core.store.CloseableIterator;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticBinningStrategy;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.entities.GeoWaveMetadata;

public class StatisticValueReader<V extends StatisticValue<R>, R> implements CloseableIterator<V> {

  private final CloseableIterator<GeoWaveMetadata> metadataIter;
  private final Statistic<V> statistic;

  private V next = null;
  private byte[] nextPrimaryId = null;

  public StatisticValueReader(
      final CloseableIterator<GeoWaveMetadata> metadataIter,
      final Statistic<V> statistic) {
    this.metadataIter = metadataIter;
    this.statistic = statistic;
  }

  @Override
  public boolean hasNext() {
    return (next != null) || metadataIter.hasNext();
  }

  @Override
  public V next() {
    V currentValue = next;
    byte[] currentPrimaryId = nextPrimaryId;
    next = null;
    nextPrimaryId = null;
    while (metadataIter.hasNext()) {
      final GeoWaveMetadata row = metadataIter.next();

      final V entry = statistic.createEmpty();
      entry.fromBinary(row.getValue());
      if (currentValue == null) {
        currentValue = entry;
        if (statistic.getBinningStrategy() != null) {
          currentValue.setBin(
              StatisticBinningStrategy.getBinFromValueId(statistic.getId(), row.getPrimaryId()));
        }
        currentPrimaryId = row.getPrimaryId();
      } else {
        if (Arrays.equals(currentPrimaryId, row.getPrimaryId())) {
          currentValue.merge(entry);
        } else {
          next = entry;
          nextPrimaryId = row.getPrimaryId();
          break;
        }
      }
    }
    return currentValue;
  }

  @Override
  public void close() {
    metadataIter.close();
  }

}
