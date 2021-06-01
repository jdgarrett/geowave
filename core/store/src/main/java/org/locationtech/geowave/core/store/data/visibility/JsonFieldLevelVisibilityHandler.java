package org.locationtech.geowave.core.store.data.visibility;

import java.io.IOException;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFieldLevelVisibilityHandler extends FieldLevelVisibilityHandler {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(JsonFieldLevelVisibilityHandler.class);
  private final ObjectMapper mapper = new ObjectMapper();

  public JsonFieldLevelVisibilityHandler() {}

  public JsonFieldLevelVisibilityHandler(final String visibilityAttribute) {
    super(visibilityAttribute);
  }

  @Override
  public String translateVisibility(final Object visibilityObject, final String fieldName) {
    if (visibilityObject == null) {
      return null;
    }
    try {
      final JsonNode attributeMap = mapper.readTree(visibilityObject.toString());
      final JsonNode field = attributeMap.get(fieldName);
      if ((field != null) && field.isValueNode()) {
        return field.textValue();
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
          return attNode.textValue();
        }
      }
    } catch (IOException | NullPointerException e) {
      LOGGER.error("Cannot parse visibility expression " + visibilityObject.toString(), e);
    }
    return null;
  }
}
