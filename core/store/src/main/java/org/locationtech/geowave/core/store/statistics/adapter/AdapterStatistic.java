package org.locationtech.geowave.core.store.statistics.adapter;

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
import org.locationtech.geowave.core.store.statistics.visibility.DefaultFieldStatisticVisibility;
import com.beust.jcommander.Parameter;

public abstract class AdapterStatistic<V extends StatisticValue<?>> extends BaseStatistic<V> {

  @Parameter(
      names = "--typeName",
      required = true,
      description = "The data type adapter for the statistic.")
  private String typeName = null;

  public AdapterStatistic(final StatisticType<V> statisticsType) {
    super(statisticsType);
  }

  public AdapterStatistic(final StatisticType<V> statisticsType, final String typeName) {
    super(statisticsType);
    this.typeName = typeName;
  }

  public void setTypeName(final String name) {
    this.typeName = name;
  }

  public final String getTypeName() {
    return typeName;
  }

  @Override
  public boolean isCompatibleWith(Class<?> adapterDataType) {
    return true;
  }

  @Override
  public final StatisticId<V> getId() {
    if (cachedStatisticId == null) {
      cachedStatisticId = new StatisticId<>(new ByteArray(typeName), getStatisticType(), getName());
    }
    return cachedStatisticId;
  }

  @Override
  public <T> EntryVisibilityHandler<T> getVisibilityHandler(
      CommonIndexModel indexModel,
      DataTypeAdapter<T> adapter) {
    return new DefaultFieldStatisticVisibility<>();
  }

  @Override
  protected int byteLength() {
    return super.byteLength()
        + VarintUtils.unsignedShortByteLength((short) typeName.getBytes().length)
        + typeName.getBytes().length;
  }

  @Override
  protected void writeBytes(ByteBuffer buffer) {
    super.writeBytes(buffer);
    VarintUtils.writeUnsignedShort((short) typeName.getBytes().length, buffer);
    buffer.put(typeName.getBytes());
  }

  @Override
  protected void readBytes(ByteBuffer buffer) {
    super.readBytes(buffer);
    byte[] nameBytes = new byte[VarintUtils.readUnsignedShort(buffer)];
    buffer.get(nameBytes);
    typeName = new String(nameBytes);
  }

}
