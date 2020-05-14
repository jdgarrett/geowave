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
import org.locationtech.geowave.core.index.ByteArrayUtils;
import org.locationtech.geowave.core.index.StringUtils;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.index.persist.PersistenceUtils;
import org.locationtech.geowave.core.store.adapter.InternalAdapterStore;
import org.locationtech.geowave.core.store.api.StatisticsOptions;
import org.locationtech.geowave.core.store.api.StatisticsQueryBuilder;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public abstract class AbstractDataStatistics<T, R>
    implements
    DataStatistics<T, R> {
  
  protected short adapterId;
  
  protected String extendedId;

  protected StatisticsOptions options;

  protected byte[] visibility;

  public AbstractDataStatistics(
      final StatisticsOptions options) {
    this.options = options;
  }

  @Override
  public StatisticsOptions getOptions() {
    return options;
  }

  @Override
  public byte[] getVisibility() {
    return visibility;
  }

  protected ByteBuffer binaryBuffer(final int size) {
    final byte stypeBytes[] = options.getStatisticsType().toBinary();
    final byte sidBytes[] = StringUtils.stringToBinary(extendedId);
    final ByteBuffer buffer =
        ByteBuffer.allocate(
            size
                + VarintUtils.unsignedShortByteLength(adapterId)
                + VarintUtils.unsignedIntByteLength(stypeBytes.length)
                + VarintUtils.unsignedIntByteLength(sidBytes.length)
                + stypeBytes.length
                + sidBytes.length);
    VarintUtils.writeUnsignedShort(adapterId, buffer);
    VarintUtils.writeUnsignedInt(stypeBytes.length, buffer);
    VarintUtils.writeUnsignedInt(sidBytes.length, buffer);
    buffer.put(stypeBytes);
    buffer.put(sidBytes);
    return buffer;
  }

  protected ByteBuffer binaryBuffer(final byte[] bytes) {
    final ByteBuffer buffer = ByteBuffer.wrap(bytes);
    adapterId = VarintUtils.readUnsignedShort(buffer);
    final int typeLength = VarintUtils.readUnsignedInt(buffer);
    final int extendedIdLength = VarintUtils.readUnsignedInt(buffer);
    final byte typeBytes[] = ByteArrayUtils.safeRead(buffer, typeLength);
    //statisticsType = new StatisticsType();
    //statisticsType.fromBinary(typeBytes);
    final byte[] extendedIdBytes = ByteArrayUtils.safeRead(buffer, extendedIdLength);
    extendedId = StringUtils.stringFromBinary(extendedIdBytes);
    return buffer;
  }

  @Override
  @SuppressWarnings("unchecked")
  public DataStatistics<T, R> duplicate() {
    return (DataStatistics<T, R>) PersistenceUtils.fromBinary(
        PersistenceUtils.toBinary(this));
  }

  @Override
  public JSONObject toJSONObject(final InternalAdapterStore store) throws JSONException {
    final JSONObject jo = new JSONObject();
    jo.put("dataType", store.getTypeName(adapterId));
    jo.put("statsType", options.getStatisticsType().getString());
    if ((extendedId != null) && !extendedId.isEmpty()) {
      jo.put("extendedId", extendedId);
    }
    jo.put(resultsName(), resultsValue());
    return jo;
  }

  protected abstract String resultsName();

  protected abstract R resultsValue();

  @Override
  public String toString() {
    return "AbstractDataStatistics [typeName="
        + adapterId
        + ", statisticsType="
        + options.getStatisticsType().getString()
        + "]";
  }
}
