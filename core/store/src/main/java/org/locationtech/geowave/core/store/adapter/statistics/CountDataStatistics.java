/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.adapter.statistics;

import java.nio.ByteBuffer;
import java.util.HashSet;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.Mergeable;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.StatisticsOptions;
import org.locationtech.geowave.core.store.callback.DeleteCallback;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;

public class CountDataStatistics extends
    AbstractDataStatistics<Object, Long> implements
    DeleteCallback<Object, GeoWaveRow> {
  public static final StatisticsType STATS_TYPE = new StatisticsType("COUNT_DATA");

  private long count = Long.MIN_VALUE;
  
  public static class Options extends StatisticsOptions {

    @Override
    public StatisticsType getStatisticsType() {
      return STATS_TYPE;
    }

    @Override
    public boolean isCompatibleWith(DataTypeAdapter<?> adapter) {
      return true;
    }

  };

  public CountDataStatistics() {
    this(new Options());
  }

  public CountDataStatistics(final Options options) {
    super(options);
  }

  public boolean isSet() {
    return count != Long.MIN_VALUE;
  }

  public long getCount() {
    return count;
  }

  @Override
  public byte[] toBinary() {
    final ByteBuffer buffer = super.binaryBuffer(VarintUtils.unsignedLongByteLength(count));
    VarintUtils.writeUnsignedLong(count, buffer);
    return buffer.array();
  }

  @Override
  public void fromBinary(final byte[] bytes) {
    final ByteBuffer buffer = super.binaryBuffer(bytes);
    count = VarintUtils.readUnsignedLong(buffer);
  }

  @Override
  public void entryIngested(final Object entry, final GeoWaveRow... kvs) {
    if (!isSet()) {
      count = 0;
    }
    count += 1;
  }

  @Override
  public void merge(final Mergeable statistics) {
    if (!isSet()) {
      count = 0;
    }
    if ((statistics != null) && (statistics instanceof CountDataStatistics)) {
      @SuppressWarnings("unchecked")
      final CountDataStatistics cStats = (CountDataStatistics) statistics;
      if (cStats.isSet()) {
        count = count + cStats.count;
      }
    }
  }

  /** This is expensive, but necessary since there may be duplicates */
  // TODO entryDeleted should only be called once with all duplicates
  private transient HashSet<ByteArray> ids = new HashSet<>();

  @Override
  public void entryDeleted(final Object entry, final GeoWaveRow... kv) {
    if (kv.length > 0) {
      if (ids.add(new ByteArray(kv[0].getDataId()))) {
        if (!isSet()) {
          count = 0;
        }
        count -= 1;
      }
    }
  }

  @Override
  public String toString() {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("count[adapterId=").append(super.getAdapterId());
    buffer.append(", count=").append(count);
    buffer.append("]");
    return buffer.toString();
  }

  @Override
  public Long getResult() {
    return count;
  }

  @Override
  protected String resultsName() {
    return "count";
  }

  @Override
  protected Long resultsValue() {
    return count;
  }
}
