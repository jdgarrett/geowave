/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.adapter.raster.stats;

import java.nio.ByteBuffer;
import org.locationtech.geowave.adapter.raster.FitToIndexGridCoverage;
import org.locationtech.geowave.adapter.raster.RasterUtils;
import org.locationtech.geowave.core.geotime.util.TWKBReader;
import org.locationtech.geowave.core.geotime.util.TWKBWriter;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.StatisticsIngestCallback;
import org.locationtech.geowave.core.store.statistics.adapter.AdapterStatistic;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.opengis.coverage.grid.GridCoverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RasterFootprintStatistics extends
    AdapterStatistic<RasterFootprintStatistics.RasterFootprintValue> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RasterFootprintStatistics.class);
  public static final StatisticType<RasterFootprintValue> STATS_TYPE = new StatisticType<>("RASTER_FOOTPRINT");

  public RasterFootprintStatistics() {
    super(STATS_TYPE);
  }

  public RasterFootprintStatistics(final String typeName) {
    super(STATS_TYPE, typeName);
  }
  
  @Override
  public boolean isCompatibleWith(final Class<?> adapterClass) {
    return GridCoverage.class.isAssignableFrom(adapterClass);
  }

  @Override
  public String getDescription() {
    return "Maintains a footprint that encompasses all of the raster data.";
  }

  @Override
  public RasterFootprintValue createEmpty() {
    return new RasterFootprintValue(this);
  }
  
  public static class RasterFootprintValue extends StatisticValue<Geometry> implements StatisticsIngestCallback {

    private RasterFootprintValue(final Statistic<?> statistic) {
      super(statistic);
    }

    private Geometry footprint = null;

    @Override
    public void merge(StatisticValue<Geometry> merge) {
      if (merge instanceof RasterFootprintValue) {
        footprint =
            RasterUtils.combineIntoOneGeometry(
                footprint,
                ((RasterFootprintValue) merge).footprint);
      }
    }

    @Override
    public <T> void entryIngested(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows) {
      if (entry instanceof FitToIndexGridCoverage) {
        footprint =
            RasterUtils.combineIntoOneGeometry(
                footprint,
                ((FitToIndexGridCoverage) entry).getFootprintWorldGeometry());
      }
    }

    @Override
    public Geometry getValue() {
      return footprint;
    }

    @Override
    public byte[] toBinary() {
      byte[] bytes = null;
      if (footprint == null) {
        bytes = new byte[] {};
      } else {
        bytes = new TWKBWriter().write(footprint);
      }
      final ByteBuffer buf = ByteBuffer.allocate(bytes.length);
      buf.put(bytes);
      return buf.array();
    }

    @Override
    public void fromBinary(byte[] bytes) {
      final ByteBuffer buf = ByteBuffer.wrap(bytes);
      final byte[] payload = buf.array();
      if (payload.length > 0) {
        try {
          footprint = new TWKBReader().read(payload);
        } catch (final ParseException e) {
          LOGGER.warn("Unable to parse WKB", e);
        }
      } else {
        footprint = null;
      }
    }
    
  }
}
