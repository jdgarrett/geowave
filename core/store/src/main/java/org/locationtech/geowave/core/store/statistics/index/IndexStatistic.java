package org.locationtech.geowave.core.store.statistics.index;


import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.store.EntryVisibilityHandler;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.index.CommonIndexModel;
import org.locationtech.geowave.core.store.statistics.BaseStatistic;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.visibility.EmptyStatisticVisibility;
import com.beust.jcommander.Parameter;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;

public abstract class IndexStatistic<R extends StatisticValue<?>> extends BaseStatistic<R> {

  @Parameter(names = "--indexName", required = true, description = "The index for the statistic.")
  private String indexName = null;

  @Parameter(
      names = "--typeName",
      description = "If specified, the statistic will only be computed for entries that belong to the given type.")
  private String typeName = null;

  public IndexStatistic(final StatisticType statisticsType) {
    this(statisticsType, null, null);
  }

  public IndexStatistic(final StatisticType statisticsType, final String indexName) {
    this(statisticsType, null, null);
  }

  public IndexStatistic(
      final StatisticType statisticsType,
      final String indexName,
      final String typeName) {
    super(statisticsType);
    this.indexName = indexName;
    this.typeName = typeName;
  }

  public void setIndexName(final String name) {
    this.indexName = name;
  }

  public String getIndexName() {
    return indexName;
  }

  public void setTypeName(final String name) {
    this.typeName = name;
    this.cachedUniqueId = null;
  }

  public String getTypeName() {
    return typeName;
  }

  @Override
  public boolean isCompatibleWith(Class<?> indexClass) {
    return true;
  }

  @Override
  public final byte[] getUniqueId() {
    if (cachedUniqueId != null) {
      return cachedUniqueId;
    }
    List<String> parts = Lists.newArrayList();
    if (typeName != null) {
      parts.add(typeName);
    }
    String uniqueId = internalUniqueId();
    if (uniqueId != null) {
      parts.add(uniqueId);
    }
    String name = getName();
    if (name != null) {
      parts.add(name);
    }
    return String.join(UNIQUE_ID_SEPARATOR, parts).getBytes();
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
    if (typeName == null) {
      length += 1;
    } else {
      length +=
          VarintUtils.unsignedShortByteLength((short) typeName.getBytes().length)
              + typeName.getBytes().length;
    }
    return length;
  }

  @Override
  protected void writeBytes(ByteBuffer buffer) {
    super.writeBytes(buffer);
    VarintUtils.writeUnsignedShort((short) indexName.getBytes().length, buffer);
    buffer.put(indexName.getBytes());
    if (typeName == null) {
      buffer.put((byte) 0);
    } else {
      VarintUtils.writeUnsignedShort((short) typeName.getBytes().length, buffer);
      buffer.put(typeName.getBytes());
    }
  }

  @Override
  protected void readBytes(ByteBuffer buffer) {
    super.readBytes(buffer);
    byte[] nameBytes = new byte[VarintUtils.readUnsignedShort(buffer)];
    buffer.get(nameBytes);
    indexName = new String(nameBytes);
    byte length = buffer.get();
    if (length == 0) {
      typeName = null;
    } else {
      nameBytes = new byte[length];
      buffer.get(nameBytes);
      typeName = new String(nameBytes);
    }
  }

}
