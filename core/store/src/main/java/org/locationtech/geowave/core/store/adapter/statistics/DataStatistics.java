/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.adapter.statistics;

import org.locationtech.geowave.core.index.Mergeable;
import org.locationtech.geowave.core.store.adapter.InternalAdapterStore;
import org.locationtech.geowave.core.store.api.StatisticsOptions;
import org.locationtech.geowave.core.store.api.StatisticsQueryBuilder;
import org.locationtech.geowave.core.store.callback.IngestCallback;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public interface DataStatistics<T, R> extends
    Mergeable,
    IngestCallback<T> {
  StatisticsOptions getOptions();

  R getResult();

  byte[] getVisibility();

  JSONObject toJSONObject(InternalAdapterStore adapterStore) throws JSONException;

  DataStatistics<T, R> duplicate();

  static <R> DataStatistics<?, R> reduce(
      final DataStatistics<?, R> a,
      final DataStatistics<?, R> b) {
    a.merge(b);
    return a;
  }
}
