import org.HdrHistogram.DoubleHistogram;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.geowave.adapter.vector.stats.NumericHistogramStatistics;
import org.locationtech.geowave.adapter.vector.stats.StatsConfig;
import org.locationtech.geowave.core.store.adapter.statistics.FieldStatisticsQueryBuilder;
import org.locationtech.geowave.core.store.api.Statistic;
import org.opengis.feature.simple.SimpleFeature;

public class FeatureNumericHistogramConfig implements StatsConfig<SimpleFeature> {
    /** */
    private static final long serialVersionUID = 6309383518148391565L;

    @Override
    public byte[] toBinary() {
      return new byte[0];
    }

    @Override
    public void fromBinary(final byte[] bytes) {}

    @Override
    public Statistic<?> create(String typeName, String fieldName) {
      return new NumericHistogramStatistics(typeName, fieldName);
    }
  }