/**
 * Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
 *
 * <p> See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Apache License, Version 2.0 which accompanies this distribution and is
 * available at http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package org.locationtech.geowave.core.store.cli.stats;

import com.beust.jcommander.Parameter;

public class StatsCommandLineOptions {

  @Parameter(names = "--indexName", description = "The name of the index, for index statistics.")
  private String indexName;
  
  @Parameter(names = "--typeName", description = "The name of the data type adapter, for field and type statistics.")
  private String typeName;
  
  @Parameter(names = "--fieldName", description = "The name of the field, for field statistics.")
  private String fieldName;
  
  @Parameter(names = "--name", description = "The name of the statistic.")
  private String name;

  @Parameter(
      names = "--auth",
      description = "The authorizations used for the statistics calculation as a subset of the accumulo user authorization; by default all authorizations are used.")
  private String authorizations;

  @Parameter(names = "--json", description = "Output in JSON format.")
  private boolean jsonFormatFlag;
  
  public StatsCommandLineOptions() {}

  public String getAuthorizations() {
    return authorizations;
  }

  public void setAuthorizations(final String authorizations) {
    this.authorizations = authorizations;
  }

  public boolean getJsonFormatFlag() {
    return jsonFormatFlag;
  }

  public void setJsonFormatFlag(final boolean jsonFormatFlag) {
    this.jsonFormatFlag = jsonFormatFlag;
  }
  
  public void setIndexName(final String indexName) {
    this.indexName = indexName;
  }
  
  public String getIndexName() {
    return indexName;
  }
  
  public void setTypeName(final String typeName) {
    this.typeName = typeName;
  }
  
  public String getTypeName() {
    return typeName;
  }
  
  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }
  
  public String getFieldName() {
    return fieldName;
  }
  
  public void setName(final String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
}
