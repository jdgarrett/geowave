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
import java.util.HashSet;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.Mergeable;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.store.adapter.PersistentAdapterStore;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Index;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.locationtech.geowave.core.store.statistics.DataStatisticsStore;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.StatisticsDeleteCallback;
import org.locationtech.geowave.core.store.statistics.StatisticsIngestCallback;
import org.locationtech.geowave.core.store.statistics.index.IndexStatistic;

public class DifferingFieldVisibilityEntryCount extends
    IndexStatistic<DifferingFieldVisibilityEntryCount.DifferingFieldVisibilityEntryCountValue> {
  public static final StatisticType STATS_TYPE = new StatisticType("DIFFERING_VISIBILITY_COUNT");


  public DifferingFieldVisibilityEntryCount() {
    super(STATS_TYPE);
  }

  public DifferingFieldVisibilityEntryCount(final String indexName) {
    super(STATS_TYPE, indexName);
  }

  public DifferingFieldVisibilityEntryCount(final String indexName, final String typeName) {
    super(STATS_TYPE, indexName, typeName);
  }

  @Override
  public String getDescription() {
    return "Counts the number of differing visibilities in the index.";
  }

  @Override
  public DifferingFieldVisibilityEntryCountValue createEmpty() {
    return new DifferingFieldVisibilityEntryCountValue();
  }

  public static class DifferingFieldVisibilityEntryCountValue implements
      StatisticValue<Long>,
      StatisticsIngestCallback,
      StatisticsDeleteCallback {

    private long entriesWithDifferingFieldVisibilities = 0;

    public boolean isAnyEntryDifferingFieldVisiblity() {
      return entriesWithDifferingFieldVisibilities > 0;
    }

    @Override
    public void merge(Mergeable merge) {
      if ((merge != null) && (merge instanceof DifferingFieldVisibilityEntryCountValue)) {
        entriesWithDifferingFieldVisibilities +=
            ((DifferingFieldVisibilityEntryCountValue) merge).entriesWithDifferingFieldVisibilities;
      }
    }

    /** This is expensive, but necessary since there may be duplicates */
    // TODO entryDeleted should only be called once with all duplicates
    private transient HashSet<ByteArray> ids = new HashSet<>();

    @Override
    public <T> void entryIngested(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows) {
      for (final GeoWaveRow kv : rows) {
        if (entryHasDifferentVisibilities(kv)) {
          if (ids.add(new ByteArray(rows[0].getDataId()))) {
            entriesWithDifferingFieldVisibilities++;
          }
        }
      }
    }

    @Override
    public <T> void entryDeleted(
        DataTypeAdapter<T> adapter,
        final T entry,
        final GeoWaveRow... kvs) {
      for (final GeoWaveRow kv : kvs) {
        if (entryHasDifferentVisibilities(kv)) {
          entriesWithDifferingFieldVisibilities--;
        }
      }
    }

    @Override
    public Long getValue() {
      return entriesWithDifferingFieldVisibilities;
    }

    @Override
    public byte[] toBinary() {
      return VarintUtils.writeUnsignedLong(entriesWithDifferingFieldVisibilities);
    }

    @Override
    public void fromBinary(byte[] bytes) {
      entriesWithDifferingFieldVisibilities = VarintUtils.readUnsignedLong(ByteBuffer.wrap(bytes));
    }

  }

  private static boolean entryHasDifferentVisibilities(final GeoWaveRow geowaveRow) {
    if ((geowaveRow.getFieldValues() != null) && (geowaveRow.getFieldValues().length > 1)) {
      // if there is 0 or 1 field, there won't be differing visibilities
      return true;
    }
    return false;
  }

  // STATS_TODO: How can we do this in a more robust way? The statistic might exist on a whole index
  // instead of with the type name specified, what about if the statistic is binned by adapter?
  public static DifferingFieldVisibilityEntryCountValue getVisibilityCounts(
      final Index index,
      final Collection<Short> adapterIdsToQuery,
      final PersistentAdapterStore adapterStore,
      final DataStatisticsStore statisticsStore,
      final String... authorizations) {
    DifferingFieldVisibilityEntryCountValue combinedVisibilityCount = null;
    for (final short adapterId : adapterIdsToQuery) {
      final DataTypeAdapter<?> adapter = adapterStore.getAdapter(adapterId);
      DifferingFieldVisibilityEntryCountValue value =
          statisticsStore.getStatisticValue(
              new DifferingFieldVisibilityEntryCount(index.getName(), adapter.getTypeName()),
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
