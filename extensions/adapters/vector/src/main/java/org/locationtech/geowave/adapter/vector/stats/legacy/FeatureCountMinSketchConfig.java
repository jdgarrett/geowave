package org.locationtech.geowave.adapter.vector.stats.legacy;

import java.nio.ByteBuffer;
import org.locationtech.geowave.adapter.vector.stats.CountMinSketchStatistic;
import org.locationtech.geowave.adapter.vector.stats.StatsConfig;
import org.locationtech.geowave.core.store.api.Statistic;
import org.opengis.feature.simple.SimpleFeature;

public class FeatureCountMinSketchConfig implements StatsConfig<SimpleFeature> {
  /** */
  private static final long serialVersionUID = 6309383518148391565L;

  private double errorFactor;
  private double probabilityOfCorrectness;

  public FeatureCountMinSketchConfig() {}

  public FeatureCountMinSketchConfig(
      final double errorFactor,
      final double probabilityOfCorrectness) {
    super();
    this.errorFactor = errorFactor;
    this.probabilityOfCorrectness = probabilityOfCorrectness;
  }

  public void setErrorFactor(final double errorFactor) {
    this.errorFactor = errorFactor;
  }

  public void setProbabilityOfCorrectness(final double probabilityOfCorrectness) {
    this.probabilityOfCorrectness = probabilityOfCorrectness;
  }

  public double getErrorFactor() {
    return errorFactor;
  }

  public double getProbabilityOfCorrectness() {
    return probabilityOfCorrectness;
  }

  @Override
  public Statistic<?> create(final String typeName, final String fieldName) {
    return new CountMinSketchStatistic(typeName, fieldName, errorFactor, probabilityOfCorrectness);
  }

  @Override
  public byte[] toBinary() {
    final ByteBuffer buf = ByteBuffer.allocate(16);
    buf.putDouble(errorFactor);
    buf.putDouble(probabilityOfCorrectness);
    return buf.array();
  }

  @Override
  public void fromBinary(final byte[] bytes) {
    final ByteBuffer buf = ByteBuffer.wrap(bytes);
    errorFactor = buf.getDouble();
    probabilityOfCorrectness = buf.getDouble();
  }
}
