/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.adapter.vector.plugin.visibility;

import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQLException;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.geowave.adapter.vector.FeatureDataAdapter;
import org.locationtech.geowave.core.index.StringUtils;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.VisibilityHandler;
import org.locationtech.geowave.core.store.data.visibility.GlobalVisibilityHandler;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

public class JsonDefinitionColumnVisibilityManagementTest {

  SimpleFeatureType type;
  DataTypeAdapter<SimpleFeature> adapter;
  List<AttributeDescriptor> descriptors;
  Object[] defaults;
  SimpleFeature newFeature;
  final JsonDefinitionColumnVisibilityManagement manager =
      new JsonDefinitionColumnVisibilityManagement();
  final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FIXED));
  final VisibilityHandler visHandler =
      manager.createVisibilityHandler("vis", new GlobalVisibilityHandler("default"));

  @Before
  public void setup() throws SchemaException, CQLException {
    type =
        DataUtilities.createType(
            "geostuff",
            "geometry:Geometry:srid=4326,vis:java.lang.String,pop:java.lang.Long,pid:String");
    adapter = new FeatureDataAdapter(type);
    descriptors = type.getAttributeDescriptors();
    defaults = new Object[descriptors.size()];
    int p = 0;
    for (final AttributeDescriptor descriptor : descriptors) {
      defaults[p++] = descriptor.getDefaultValue();
    }

    newFeature = SimpleFeatureBuilder.build(type, defaults, UUID.randomUUID().toString());
    newFeature.setAttribute("pop", Long.valueOf(100));
    newFeature.setAttribute("pid", UUID.randomUUID().toString());
    newFeature.setAttribute("vis", "{\"pid\":\"TS\", \"geo.*\":\"S\"}");
    newFeature.setAttribute("geometry", factory.createPoint(new Coordinate(43.454, 128.232)));
  }

  @Test
  public void testPIDNonDefault() {

    assertTrue(
        Arrays.equals(
            "TS".getBytes(StringUtils.getGeoWaveCharset()),
            visHandler.getVisibility(adapter, newFeature, "pid")));
  }

  @Test
  public void testPOPNonDefault() {
    assertTrue(
        Arrays.equals(
            "default".getBytes(StringUtils.getGeoWaveCharset()),
            visHandler.getVisibility(adapter, newFeature, "pop")));
  }

  @Test
  public void testGEORegexDefault() {
    assertTrue(
        Arrays.equals(
            "S".getBytes(StringUtils.getGeoWaveCharset()),
            visHandler.getVisibility(adapter, newFeature, "geometry")));
  }

  @Test
  public void testCatchAllRegexDefault() {
    newFeature.setAttribute("vis", "{\"pid\":\"TS\", \".*\":\"U\"}");
    assertTrue(
        Arrays.equals(
            "U".getBytes(StringUtils.getGeoWaveCharset()),
            visHandler.getVisibility(adapter, newFeature, "pop")));
  }
}
