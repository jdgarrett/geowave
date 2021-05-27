/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.adapter.vector.plugin.visibility;

import java.io.IOException;
import java.util.Iterator;
import org.locationtech.geowave.core.index.StringUtils;
import org.locationtech.geowave.core.store.api.VisibilityHandler;
import org.locationtech.geowave.core.store.data.visibility.FallbackVisibilityHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Object defining visibility is a json structure where each attribute defines the visibility for a
 * field with the same name (as the attribute).
 *
 * <p> Example: { "geometry" : "S", "eventName": "TS"}
 *
 * <p> Json attributes can also be regular expressions, matching more than one field name.
 *
 * <p> Example: { "geo.*" : "S", ".*" : "TS"}.
 *
 * <p> The order of the expression must be considered if one expression is more general than
 * another, as shown in the example. The expression ".*" matches all attributes. The more specific
 * expression "geo.*" must be ordered first.
 */
public class JsonDefinitionColumnVisibilityManagement implements ColumnVisibilityManagementSpi {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(JsonDefinitionColumnVisibilityManagement.class);

  public static class JsonDefinitionFieldLevelVisibilityHandler extends
      FieldLevelVisibilityHandler {

    public JsonDefinitionFieldLevelVisibilityHandler() {}

    public JsonDefinitionFieldLevelVisibilityHandler(final String visibilityAttribute) {
      super(visibilityAttribute);
    }

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] translateVisibility(final Object visibilityObject, final String fieldName) {
      if (visibilityObject == null) {
        return null;
      }
      try {
        final JsonNode attributeMap = mapper.readTree(visibilityObject.toString());
        final JsonNode field = attributeMap.get(fieldName);
        if ((field != null) && field.isValueNode()) {
          return validate(field.textValue());
        }
        final Iterator<String> attNameIt = attributeMap.fieldNames();
        while (attNameIt.hasNext()) {
          final String attName = attNameIt.next();
          if (fieldName.matches(attName)) {
            final JsonNode attNode = attributeMap.get(attName);
            if (attNode == null) {
              LOGGER.error(
                  "Cannot parse visibility expression, JsonNode for attribute "
                      + attName
                      + " was null");
              return null;
            }
            return validate(attNode.textValue());
          }
        }
      } catch (IOException | NullPointerException e) {
        LOGGER.error("Cannot parse visibility expression " + visibilityObject.toString(), e);
      }
      return null;
    }

    protected byte[] validate(final String vis) {
      return StringUtils.stringToBinary(vis);

      // TODO come up with another way to validate, below is the accumulo
      // dependent validation

      // try {
      // ColumnVisibility cVis = new ColumnVisibility(
      // vis);
      // return cVis.getExpression();
      // }
      // catch (Exception ex) {
      // LOGGER.error(
      // "Failed to parse visibility " + vis,
      // ex);
      // return null;
      // }
    }
  }

  @Override
  public VisibilityHandler createVisibilityHandler(
      final String visibilityAttributeName,
      final VisibilityHandler fallbackHandler) {
    if (fallbackHandler != null) {
      return new FallbackVisibilityHandler(
          new VisibilityHandler[] {
              new JsonDefinitionFieldLevelVisibilityHandler(visibilityAttributeName),
              fallbackHandler});
    }
    return new JsonDefinitionFieldLevelVisibilityHandler(visibilityAttributeName);
  }
}
