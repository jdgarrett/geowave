package org.locationtech.geowave.core.store.statistics;

import java.nio.ByteBuffer;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticBinningStrategy;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import com.beust.jcommander.Parameter;

public abstract class BaseStatistic<V extends StatisticValue<?>> implements Statistic<V> {

  @Parameter(names = "--name", description = "The name of the statistic.")
  private String name = null;

  private final StatisticType<V> statisticType;

  private StatisticBinningStrategy binningStrategy = null;
  
  protected StatisticId<V> cachedStatisticId = null;

  public BaseStatistic(final StatisticType<V> statisticType) {
    this.statisticType = statisticType;
  }

  public void setName(final String name) {
    this.name = name;
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
  public final StatisticType<V> getStatisticType() {
    return statisticType;
  }

  protected int byteLength() {
    if (name == null) {
      return VarintUtils.unsignedShortByteLength((short) 0);
    }
    return VarintUtils.unsignedShortByteLength((short) name.getBytes().length)
        + name.getBytes().length;
  }

  protected void writeBytes(ByteBuffer buffer) {
    if (name == null) {
      VarintUtils.writeUnsignedShort((short) 0, buffer);
    } else {
      VarintUtils.writeUnsignedShort((short) name.getBytes().length, buffer);
      buffer.put(name.getBytes());
    }
  }

  protected void readBytes(ByteBuffer buffer) {
    short length = VarintUtils.readUnsignedShort(buffer);
    if (length > 0) {
      byte[] nameBytes = new byte[length];
      name = new String(nameBytes);
    } else {
      name = null;
    }
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
