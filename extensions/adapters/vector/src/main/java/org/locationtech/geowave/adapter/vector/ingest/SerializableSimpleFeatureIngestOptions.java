package org.locationtech.geowave.adapter.vector.ingest;

import com.beust.jcommander.ParametersDelegate;

public class SerializableSimpleFeatureIngestOptions extends SimpleFeatureIngestOptions {

  @ParametersDelegate
  private FeatureSerializationOptionProvider serializationFormatOptionProvider =
      new FeatureSerializationOptionProvider();

  public FeatureSerializationOptionProvider getSerializationFormatOptionProvider() {
    return serializationFormatOptionProvider;
  }

  public void setSerializationFormatOptionProvider(
      final FeatureSerializationOptionProvider serializationFormatOptionProvider) {
    this.serializationFormatOptionProvider = serializationFormatOptionProvider;
  }

}
