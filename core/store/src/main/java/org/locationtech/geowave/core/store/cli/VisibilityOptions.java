/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.cli;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.bouncycastle.util.Strings;
import org.locationtech.geowave.core.index.StringUtils;
import org.locationtech.geowave.core.store.api.VisibilityHandler;
import org.locationtech.geowave.core.store.data.visibility.FallbackVisibilityHandler;
import org.locationtech.geowave.core.store.data.visibility.FieldMappedVisibilityHandler;
import org.locationtech.geowave.core.store.data.visibility.GlobalVisibilityHandler;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Maps;

public class VisibilityOptions implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  @Parameter(
      names = {"-v", "--visibility"},
      description = "The global visibility of the data ingested (optional; if not specified, the data will be unrestricted)")
  private String visibility = null;

  @Parameter(
      names = {"-fv", "--fieldVisibility"},
      description = "Specify the visibility of a specific field in the format `<fieldName>:<visibility>`.  This option can be specified multiple times for different fields.")
  private List<String> fieldVisibilities = Lists.newArrayList();

  public String getGlobalVisibility() {
    return visibility;
  }

  public void setGlobalVisibility(final String visibility) {
    this.visibility = visibility;
  }

  public List<String> getFieldVisibilities() {
    return fieldVisibilities;
  }

  public void setFieldVisibilities(final List<String> fieldVisibilities) {
    this.fieldVisibilities = fieldVisibilities;
  }

  public void addFieldVisiblity(final String fieldName, final String visibility) {
    fieldVisibilities.add(fieldName + ":" + visibility);
  }

  public VisibilityHandler getConfiguredVisibilityHandler() {
    final VisibilityHandler globalVisibilityHandler;
    if (visibility != null && visibility.trim().length() > 0) {
      globalVisibilityHandler = new GlobalVisibilityHandler(visibility.trim());
    } else {
      globalVisibilityHandler = null;
    }
    final VisibilityHandler fieldVisibilityHandler = parseFieldVisibilities();
    if (fieldVisibilityHandler == null) {
      if (globalVisibilityHandler != null) {
        return globalVisibilityHandler;
      }
      return null;
    } else if (globalVisibilityHandler != null) {
      return new FallbackVisibilityHandler(
          new VisibilityHandler[] {fieldVisibilityHandler, globalVisibilityHandler});
    }
    return fieldVisibilityHandler;
  }

  private VisibilityHandler parseFieldVisibilities() {
    if (fieldVisibilities.size() == 0) {
      return null;
    }
    final Map<String, byte[]> fieldVisMap =
        Maps.newHashMapWithExpectedSize(fieldVisibilities.size());
    for (final String vis : fieldVisibilities) {
      final String[] split = Strings.split(vis, ':');
      if (split.length != 2) {
        throw new ParameterException(
            "Error parsing field visibility '"
                + vis
                + "', expected format <fieldName>:<visibility>.");
      }
      fieldVisMap.put(split[0], StringUtils.stringToBinary(split[1]));
    }
    return new FieldMappedVisibilityHandler(fieldVisMap);
  }
}
