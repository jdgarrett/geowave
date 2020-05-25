/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.data.visibility;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.ByteArrayUtils;
import org.locationtech.geowave.core.index.Mergeable;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.store.adapter.PersistentAdapterStore;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.locationtech.geowave.core.store.entities.GeoWaveValue;
import org.locationtech.geowave.core.store.statistics.DataStatisticsStore;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.StatisticsDeleteCallback;
import org.locationtech.geowave.core.store.statistics.StatisticsIngestCallback;
import org.locationtech.geowave.core.store.statistics.index.IndexStatistic;
import org.locationtech.geowave.core.store.util.VisibilityExpression;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

// STATS_TODO: This could really just be a count statistic that uses a binning strategy based on
// visibility.
public class FieldVisibilityCount extends
    IndexStatistic<FieldVisibilityCount.FieldVisibilityCountValue> {
  public static final StatisticType STATS_TYPE = new StatisticType("FIELD_VISIBILITY_COUNT");

  public FieldVisibilityCount() {
    super(STATS_TYPE);
  }

  public FieldVisibilityCount(final String indexName) {
    super(STATS_TYPE, indexName);
  }

  public FieldVisibilityCount(final String indexName, final String typeName) {
    super(STATS_TYPE, typeName);
  }

  @Override
  public String getDescription() {
    return "Counts the number of entries for each field visibility.";
  }

  @Override
  public FieldVisibilityCountValue createEmpty() {
    return new FieldVisibilityCountValue();
  }

  public static class FieldVisibilityCountValue implements
      StatisticValue<Map<ByteArray, Long>>,
      StatisticsIngestCallback,
      StatisticsDeleteCallback {
    private final Map<ByteArray, Long> countsPerVisibility = Maps.newHashMap();

    public boolean isAuthorizationsLimiting(final String... authorizations) {
      final Set<String> set = Sets.newHashSet(authorizations);
      for (final Entry<ByteArray, Long> vis : countsPerVisibility.entrySet()) {
        if ((vis.getValue() > 0)
            && (vis.getKey() != null)
            && (vis.getKey().getBytes().length > 0)
            && !VisibilityExpression.evaluate(vis.getKey().getString(), set)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void merge(Mergeable merge) {
      if ((merge != null) && (merge instanceof FieldVisibilityCountValue)) {
        final Map<ByteArray, Long> otherCounts =
            ((FieldVisibilityCountValue) merge).countsPerVisibility;
        for (final Entry<ByteArray, Long> entry : otherCounts.entrySet()) {
          Long count = countsPerVisibility.get(entry.getKey());
          if (count == null) {
            count = 0L;
          }
          countsPerVisibility.put(entry.getKey(), count + entry.getValue());
        }
      }
    }

    @Override
    public <T> void entryDeleted(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows) {
      updateEntry(-1, rows);
    }

    @Override
    public <T> void entryIngested(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows) {
      updateEntry(1, rows);
    }

    private void updateEntry(final int incrementValue, final GeoWaveRow... kvs) {
      for (final GeoWaveRow row : kvs) {
        final GeoWaveValue[] values = row.getFieldValues();
        for (final GeoWaveValue v : values) {
          ByteArray visibility = new ByteArray(new byte[] {});
          if (v.getVisibility() != null) {
            visibility = new ByteArray(v.getVisibility());
          }
          Long count = countsPerVisibility.get(visibility);
          if (count == null) {
            count = 0L;
          }
          countsPerVisibility.put(visibility, count + incrementValue);
        }
      }
    }

    @Override
    public Map<ByteArray, Long> getValue() {
      return countsPerVisibility;
    }

    @Override
    public byte[] toBinary() {
      int bufferSize = 0;
      int serializedCounts = 0;
      for (final Entry<ByteArray, Long> entry : countsPerVisibility.entrySet()) {
        if (entry.getValue() != 0) {
          bufferSize += VarintUtils.unsignedIntByteLength(entry.getKey().getBytes().length);
          bufferSize += entry.getKey().getBytes().length;
          bufferSize += VarintUtils.unsignedLongByteLength(entry.getValue());
          serializedCounts++;
        }
      }
      bufferSize += VarintUtils.unsignedIntByteLength(serializedCounts);
      final ByteBuffer buf = ByteBuffer.allocate(bufferSize);
      VarintUtils.writeUnsignedInt(serializedCounts, buf);
      for (final Entry<ByteArray, Long> entry : countsPerVisibility.entrySet()) {
        if (entry.getValue() != 0) {
          VarintUtils.writeUnsignedInt(entry.getKey().getBytes().length, buf);
          buf.put(entry.getKey().getBytes());
          VarintUtils.writeUnsignedLong(entry.getValue(), buf);
        }
      }
      return buf.array();
    }

    @Override
    public void fromBinary(byte[] bytes) {
      final ByteBuffer buf = ByteBuffer.wrap(bytes);
      final int size = VarintUtils.readUnsignedInt(buf);
      ByteArrayUtils.verifyBufferSize(buf, size);
      countsPerVisibility.clear();
      for (int i = 0; i < size; i++) {
        final int idCount = VarintUtils.readUnsignedInt(buf);
        final byte[] id = ByteArrayUtils.safeRead(buf, idCount);
        final long count = VarintUtils.readUnsignedLong(buf);
        if (count != 0) {
          countsPerVisibility.put(new ByteArray(id), count);
        }
      }
    }
  }

  public static FieldVisibilityCountValue getVisibilityCounts(
      final Index index,
      final Collection<Short> adapterIdsToQuery,
      final PersistentAdapterStore adapterStore,
      final DataStatisticsStore statisticsStore,
      final String... authorizations) {
    FieldVisibilityCountValue combinedVisibilityCount = null;
    for (final short adapterId : adapterIdsToQuery) {
      DataTypeAdapter<?> adapter = adapterStore.getAdapter(adapterId);
      FieldVisibilityCountValue value =
          statisticsStore.getStatisticValue(
              new FieldVisibilityCount(index.getName(), adapter.getTypeName()),
              authorizations);
      if (combinedVisibilityCount == null) {
        combinedVisibilityCount = value;
      } else {
        combinedVisibilityCount.merge(value);
      }
    }
    return combinedVisibilityCount;
  }
}
