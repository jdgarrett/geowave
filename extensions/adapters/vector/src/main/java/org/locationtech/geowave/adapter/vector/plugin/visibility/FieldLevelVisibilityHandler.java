/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.adapter.vector.plugin.visibility;

import org.locationtech.geowave.core.index.StringUtils;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.VisibilityHandler;
import org.locationtech.geowave.core.store.data.visibility.VisibilityManagement;
import org.opengis.feature.simple.SimpleFeature;

/**
 * VIS_TODO: Update comments
 * 
 * @param <T>
 */
public class FieldLevelVisibilityHandler implements VisibilityHandler {

  private String visibilityAttribute;

  public FieldLevelVisibilityHandler() {}

  public FieldLevelVisibilityHandler(final String visibilityAttribute) {
    super();
    this.visibilityAttribute = visibilityAttribute;
  }

  protected byte[] translateVisibility(final Object visibilityObject, final String fieldName) {
    if (visibilityObject == null) {
      return null;
    }
    return StringUtils.stringToBinary(visibilityObject.toString());
  }

  @Override
  public <T> byte[] getVisibility(
      final DataTypeAdapter<T> adapter,
      final T entry,
      final String fieldName) {

    final Object visibilityAttributeValue = adapter.getFieldValue(entry, visibilityAttribute);
    return translateVisibility(visibilityAttributeValue, fieldName);
  }

  @Override
  public byte[] toBinary() {
    return StringUtils.stringToBinary(visibilityAttribute);
  }

  @Override
  public void fromBinary(byte[] bytes) {
    visibilityAttribute = StringUtils.stringFromBinary(bytes);
  }
}
