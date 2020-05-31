package org.locationtech.geowave.core.store.statistics.field;

import java.nio.ByteBuffer;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.store.EntryVisibilityHandler;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.index.CommonIndexModel;
import org.locationtech.geowave.core.store.statistics.BaseStatistic;
import org.locationtech.geowave.core.store.statistics.FieldStatisticId;
import org.locationtech.geowave.core.store.statistics.StatisticId;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.visibility.FieldNameStatisticVisibility;
import com.beust.jcommander.Parameter;

public abstract class FieldStatistic<V extends StatisticValue<?>> extends BaseStatistic<V> {

  @Parameter(
      names = "--typeName",
      required = true,
      description = "The data type that contains the field for the statistic.")
  private String typeName = null;

  @Parameter(
      names = "--fieldName",
      required = true,
      description = "The field name to use for statistics.")
  private String fieldName = null;

  public FieldStatistic(final StatisticType<V> statisticsType) {
    this(statisticsType, null, null);
  }

  public FieldStatistic(
      final StatisticType<V> statisticsType,
      final String typeName,
      final String fieldName) {
    super(statisticsType);
    this.typeName = typeName;
    this.fieldName = fieldName;
  }

  public void setTypeName(final String name) {
    this.typeName = name;
  }

  public final String getTypeName() {
    return typeName;
  }

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  public String getFieldName() {
    return this.fieldName;
  }

  @Override
  public abstract boolean isCompatibleWith(Class<?> fieldClass);

  @Override
  public final StatisticId<V> getId() {
    if (cachedStatisticId == null) {
      cachedStatisticId = new FieldStatisticId<>(new ByteArray(typeName), getStatisticType(), fieldName, getName());
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
    return new FieldNameStatisticVisibility<>(fieldName, indexModel, adapter);
  }

  @Override
  protected int byteLength() {
    return super.byteLength()
        + VarintUtils.unsignedShortByteLength((short) typeName.getBytes().length)
        + VarintUtils.unsignedShortByteLength((short) fieldName.getBytes().length)
        + typeName.length()
        + fieldName.getBytes().length;
  }

  @Override
  protected void writeBytes(ByteBuffer buffer) {
    super.writeBytes(buffer);
    VarintUtils.writeUnsignedShort((short) typeName.getBytes().length, buffer);
    buffer.put(typeName.getBytes());
    VarintUtils.writeUnsignedShort((short) fieldName.getBytes().length, buffer);
    buffer.put(fieldName.getBytes());
  }

  @Override
  protected void readBytes(ByteBuffer buffer) {
    super.readBytes(buffer);
    byte[] typeBytes = new byte[VarintUtils.readUnsignedShort(buffer)];
    buffer.get(typeBytes);
    byte[] nameBytes = new byte[VarintUtils.readUnsignedShort(buffer)];
    buffer.get(nameBytes);
    typeName = new String(typeBytes);
    fieldName = new String(nameBytes);
  }

}
