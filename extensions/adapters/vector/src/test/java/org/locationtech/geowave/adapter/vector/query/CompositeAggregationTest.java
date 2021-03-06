/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.adapter.vector.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
import org.locationtech.geowave.adapter.vector.query.aggregation.CompositeVectorAggregation;
import org.locationtech.geowave.adapter.vector.query.aggregation.VectorCountAggregation;
import org.locationtech.geowave.core.geotime.binning.SpatialBinningType;
import org.locationtech.geowave.core.geotime.store.query.aggregate.AbstractVectorAggregationTest;
import org.locationtech.geowave.core.geotime.store.query.aggregate.FieldNameParam;
import org.locationtech.geowave.core.geotime.store.query.aggregate.SpatialSimpleFeatureBinningStrategy;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.persist.PersistableList;
import org.locationtech.geowave.core.store.api.Aggregation;
import org.locationtech.geowave.core.store.query.aggregate.BinningAggregation;
import org.locationtech.geowave.core.store.query.aggregate.BinningAggregationOptions;
import org.opengis.feature.simple.SimpleFeature;

public class CompositeAggregationTest extends AbstractVectorAggregationTest {

  @Test
  public void testCompositeAggregation() {
    final List<SimpleFeature> features = generateFeatures();
    final CompositeVectorAggregation aggregation = new CompositeVectorAggregation();
    aggregation.add(new VectorCountAggregation(null));
    aggregation.add(new VectorCountAggregation(new FieldNameParam(GEOMETRY_COLUMN)));
    aggregation.add(new VectorCountAggregation(new FieldNameParam(ALL_NULL_COLUMN)));
    aggregation.add(new VectorCountAggregation(new FieldNameParam(ODDS_NULL_COLUMN)));

    final List<Object> result = aggregateObjects(aggregation, features);
    assertEquals(4, result.size());
    assertTrue(result.get(0) instanceof Long);
    assertEquals(Long.valueOf(features.size()), result.get(0));
    assertTrue(result.get(1) instanceof Long);
    assertEquals(Long.valueOf(features.size()), result.get(1));
    assertTrue(result.get(2) instanceof Long);
    assertEquals(Long.valueOf(0L), result.get(2));
    assertTrue(result.get(3) instanceof Long);
    assertEquals(Long.valueOf((features.size() / 2) + 1), result.get(3));
  }

  @Test
  public void testCompositeAggregationWithBinning() {
    final List<SimpleFeature> features = generateFeatures();
    final CompositeVectorAggregation compositeAggregation = new CompositeVectorAggregation();
    compositeAggregation.add(new VectorCountAggregation(null));
    compositeAggregation.add(new VectorCountAggregation(new FieldNameParam(GEOMETRY_COLUMN)));
    compositeAggregation.add(new VectorCountAggregation(new FieldNameParam(ALL_NULL_COLUMN)));
    compositeAggregation.add(new VectorCountAggregation(new FieldNameParam(ODDS_NULL_COLUMN)));
    final Aggregation<BinningAggregationOptions<PersistableList, SimpleFeature>, Map<ByteArray, List<Object>>, SimpleFeature> compositeBinningAggregation =
        new BinningAggregation<>(
            compositeAggregation,
            new SpatialSimpleFeatureBinningStrategy(SpatialBinningType.S2, 4, true),
            -1);
    final Aggregation<BinningAggregationOptions<FieldNameParam, SimpleFeature>, Map<ByteArray, Long>, SimpleFeature> simpleBinningAggregation =
        new BinningAggregation<>(
            new VectorCountAggregation(new FieldNameParam(GEOMETRY_COLUMN)),
            new SpatialSimpleFeatureBinningStrategy(SpatialBinningType.S2, 4, true),
            -1);
    final Map<ByteArray, List<Object>> compositeBinningResult =
        aggregateObjects(compositeBinningAggregation, features);
    final Map<ByteArray, Long> simpleBinningResult =
        aggregateObjects(simpleBinningAggregation, features);
    final List<Object> compositeResult = aggregateObjects(compositeAggregation, features);

    // first make sure each key for simple binning match the count of the corresponding composite
    // binning field
    assertEquals(simpleBinningResult.size(), compositeBinningResult.size());
    List<Object> aggregateBinningResult = null;
    for (final Entry<ByteArray, List<Object>> obj : compositeBinningResult.entrySet()) {
      final Long simpleResult = simpleBinningResult.get(obj.getKey());
      assertEquals(simpleResult, obj.getValue().get(1));
      if (aggregateBinningResult == null) {
        aggregateBinningResult = new ArrayList<>(obj.getValue());
      } else {
        aggregateBinningResult = compositeAggregation.merge(aggregateBinningResult, obj.getValue());
      }
    }
    // then make sure that aggregating the keys on the composite binning matches the non-binning
    // result
    assertEquals(compositeResult, aggregateBinningResult);
  }
}
