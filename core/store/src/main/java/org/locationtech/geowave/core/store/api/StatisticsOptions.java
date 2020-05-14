package org.locationtech.geowave.core.store.api;

import java.nio.ByteBuffer;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.index.persist.Persistable;
import org.locationtech.geowave.core.store.adapter.statistics.StatisticsType;
import com.beust.jcommander.Parameter;

public abstract class StatisticsOptions implements Persistable {
  
  @Parameter(names = "--name", description = "The name of the statistic.")
  private String name = null;
  
  public abstract StatisticsType getStatisticsType();
  
  public abstract boolean isCompatibleWith(DataTypeAdapter<?> adapter);
  
  public void setName(final String name) {
    this.name = name;
  }
  
  public final String getName() {
    return name;
  }
  
  protected final String generateName() {
    final String extendedId = getExtendedId();
    if (extendedId == null) {
      return getStatisticsType().getString();
    }
    return getStatisticsType().getString() + '_' + getExtendedId();
  }
  
  public String getExtendedId() {
    return null;
  }
  
  protected int byteLength() {
    return VarintUtils.unsignedShortByteLength((short) name.getBytes().length) + name.getBytes().length;
  }
  
  protected void writeBytes(ByteBuffer buffer) {
    VarintUtils.writeUnsignedShort((short) name.getBytes().length, buffer);
    buffer.put(name.getBytes());
  }
  
  protected void readBytes(ByteBuffer buffer) {
    byte[] nameBytes = new byte[VarintUtils.readUnsignedShort(buffer)];
    name = new String(nameBytes);
  }
  
  @Override
  public final byte[] toBinary() {
    ByteBuffer buffer = ByteBuffer.allocate(byteLength());
    writeBytes(buffer);
    return buffer.array();
  }

  @Override
  public final void fromBinary(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    readBytes(buffer);
  }
}
