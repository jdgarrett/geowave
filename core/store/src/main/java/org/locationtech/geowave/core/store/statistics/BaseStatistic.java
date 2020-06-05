package org.locationtech.geowave.core.store.statistics;

import java.nio.ByteBuffer;
import org.locationtech.geowave.core.index.StringUtils;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.index.persist.PersistenceUtils;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticBinningStrategy;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import com.beust.jcommander.Parameter;

public abstract class BaseStatistic<V extends StatisticValue<?>> implements Statistic<V> {

  @Parameter(names = "--tag", description = "A tag for the statistic.")
  private String tag = "default";

  private final StatisticType<V> statisticType;

  private StatisticBinningStrategy binningStrategy = null;

  protected StatisticId<V> cachedStatisticId = null;

  public BaseStatistic(final StatisticType<V> statisticType) {
    this.statisticType = statisticType;
  }

  public void setTag(final String tag) {
    this.tag = tag;
  }

  public void setInternal() {
    this.tag = INTERNAL_TAG;
  }

  @Override
  public final String getTag() {
    return tag;
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

  private byte[] binningStrategyBytesCache = null;

  protected int byteLength() {
    binningStrategyBytesCache = PersistenceUtils.toBinary(binningStrategy);
    return VarintUtils.unsignedShortByteLength((short) binningStrategyBytesCache.length)
        + binningStrategyBytesCache.length
        + VarintUtils.unsignedShortByteLength((short) tag.length())
        + tag.length();
  }

  protected void writeBytes(ByteBuffer buffer) {
    if (binningStrategyBytesCache == null) {
      binningStrategyBytesCache = PersistenceUtils.toBinary(binningStrategy);
    }
    VarintUtils.writeUnsignedShort((short) binningStrategyBytesCache.length, buffer);
    buffer.put(binningStrategyBytesCache);
    binningStrategyBytesCache = null;
    byte[] stringBytes = StringUtils.stringToBinary(tag);
    VarintUtils.writeUnsignedShort((short) stringBytes.length, buffer);
    buffer.put(stringBytes);
  }

  protected void readBytes(ByteBuffer buffer) {
    short length = VarintUtils.readUnsignedShort(buffer);
    binningStrategyBytesCache = new byte[length];
    buffer.get(binningStrategyBytesCache);
    binningStrategy =
        (StatisticBinningStrategy) PersistenceUtils.fromBinary(binningStrategyBytesCache);
    binningStrategyBytesCache = null;
    length = VarintUtils.readUnsignedShort(buffer);
    byte[] tagBytes = new byte[length];
    buffer.get(tagBytes);
    tag = StringUtils.stringFromBinary(tagBytes);
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
