package org.locationtech.geowave.core.store.statistics.index;

import java.nio.ByteBuffer;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.store.EntryVisibilityHandler;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.index.CommonIndexModel;
import org.locationtech.geowave.core.store.statistics.BaseStatistic;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.visibility.EmptyStatisticVisibility;
import com.beust.jcommander.Parameter;

public abstract class IndexStatistic<V extends StatisticValue<?>> extends BaseStatistic<V> {

  @Parameter(names = "--indexName", required = true, description = "The index for the statistic.")
  private String indexName = null;

  public IndexStatistic(final StatisticType<V> statisticsType) {
    this(statisticsType, null);
  }

  public IndexStatistic(
      final StatisticType<V> statisticsType,
      final String indexName) {
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
      cachedStatisticId = generateStatisticId(indexName, getStatisticType(), getName());
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
    int length =
        super.byteLength()
            + VarintUtils.unsignedShortByteLength((short) indexName.getBytes().length)
            + indexName.getBytes().length;
    return length;
  }

  @Override
  protected void writeBytes(ByteBuffer buffer) {
    super.writeBytes(buffer);
    VarintUtils.writeUnsignedShort((short) indexName.getBytes().length, buffer);
    buffer.put(indexName.getBytes());
  }

  @Override
  protected void readBytes(ByteBuffer buffer) {
    super.readBytes(buffer);
    byte[] nameBytes = new byte[VarintUtils.readUnsignedShort(buffer)];
    buffer.get(nameBytes);
    indexName = new String(nameBytes);
  }

  public static <V extends StatisticValue<?>> StatisticId<V> generateStatisticId(
      final String indexName,
      final StatisticType<V> statisticType,
      final String name) {
    return new StatisticId<>(new ByteArray(indexName), statisticType, name);
  }

}
