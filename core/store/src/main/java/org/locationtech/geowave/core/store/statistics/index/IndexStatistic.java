package org.locationtech.geowave.core.store.statistics.index;

import java.nio.ByteBuffer;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.StringUtils;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.store.EntryVisibilityHandler;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.index.CommonIndexModel;
import org.locationtech.geowave.core.store.statistics.BaseStatistic;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import org.locationtech.geowave.core.store.statistics.visibility.EmptyStatisticVisibility;
import com.beust.jcommander.Parameter;

public abstract class IndexStatistic<V extends StatisticValue<?>> extends BaseStatistic<V> {

  @Parameter(names = "--indexName", required = true, description = "The index for the statistic.")
  private String indexName = null;

  public IndexStatistic(final IndexStatisticType<V> statisticsType) {
    this(statisticsType, null);
  }

  public IndexStatistic(final IndexStatisticType<V> statisticsType, final String indexName) {
    super(statisticsType);
    this.indexName = indexName;
  }

  public void setIndexName(final String name) {
    this.indexName = name;
  }

  public String getIndexName() {
    return indexName;
  }

  @Override
  public boolean isCompatibleWith(Class<?> indexClass) {
    return true;
  }

  @Override
  public final StatisticId<V> getId() {
    if (cachedStatisticId == null) {
      cachedStatisticId =
          generateStatisticId(indexName, (IndexStatisticType<V>) getStatisticType(), getTag());
    }
    return cachedStatisticId;
  }

  /**
   * 
   * @return
   */
  protected String internalUniqueId() {
    return null;
  }

  @Override
  public <T> EntryVisibilityHandler<T> getVisibilityHandler(
      CommonIndexModel indexModel,
      DataTypeAdapter<T> adapter) {
    return new EmptyStatisticVisibility<>();
  }

  @Override
  protected int byteLength() {
    return super.byteLength()
        + VarintUtils.unsignedShortByteLength((short) indexName.length())
        + indexName.length();
  }

  @Override
  protected void writeBytes(ByteBuffer buffer) {
    super.writeBytes(buffer);
    VarintUtils.writeUnsignedShort((short) indexName.length(), buffer);
    buffer.put(StringUtils.stringToBinary(indexName));
  }

  @Override
  protected void readBytes(ByteBuffer buffer) {
    super.readBytes(buffer);
    byte[] nameBytes = new byte[VarintUtils.readUnsignedShort(buffer)];
    buffer.get(nameBytes);
    indexName = StringUtils.stringFromBinary(nameBytes);
  }


  @Override
  public String toString() {
    final StringBuffer buffer = new StringBuffer();
    buffer.append(getStatisticType().getString()).append("[index=").append(indexName).append("]");
    return buffer.toString();
  }


  public static <V extends StatisticValue<?>> StatisticId<V> generateStatisticId(
      final String indexName,
      final IndexStatisticType<V> statisticType,
      final String tag) {
    return new StatisticId<>(generateGroupId(indexName), statisticType, tag);
  }

  public static ByteArray generateGroupId(final String indexName) {
    return new ByteArray("I" + indexName);
  }

}
