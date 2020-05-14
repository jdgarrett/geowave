package org.locationtech.geowave.core.store.adapter.statistics;

import org.locationtech.geowave.core.store.api.StatisticsOptions;
import com.beust.jcommander.Parameter;

public abstract class FieldStatisticsOptions extends StatisticsOptions {
  
  @Parameter(names = "--fieldName", description = "Field name.")
  private String fieldName = null;
  
  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }
  
  public String getFieldName() {
    return this.fieldName;
  }

}
