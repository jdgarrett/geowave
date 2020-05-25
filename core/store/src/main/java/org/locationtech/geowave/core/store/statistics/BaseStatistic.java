package org.locationtech.geowave.core.store.statistics;

import java.nio.ByteBuffer;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticBinningStrategy;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import com.beust.jcommander.Parameter;

public abstract class BaseStatistic<R extends StatisticValue<?>> implements Statistic<R> {

  public static String UNIQUE_ID_SEPARATOR = "|";

  @Parameter(names = "--name", description = "The name of the statistic.")
  private String name = null;

  private final StatisticType statisticType;

  private StatisticBinningStrategy binningStrategy = null;

  protected byte[] cachedUniqueId = null;

  public BaseStatistic(final StatisticType statisticType) {
    this.statisticType = statisticType;
  }

  public void setName(final String name) {
    this.name = name;
    this.cachedUniqueId = null;
  }

  @Override
  public final String getName() {
    return name;
  }

  public void setBinningStrategy(final StatisticBinningStrategy binningStrategy) {
    this.binningStrategy = binningStrategy;
  }

  @Override
  public StatisticBinningStrategy getBinningStrategy() {
    return binningStrategy;
  }

  @Override
  public final StatisticType getStatisticType() {
    return statisticType;
  }

  protected int byteLength() {
    return VarintUtils.unsignedShortByteLength((short) name.getBytes().length)
        + name.getBytes().length;
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
