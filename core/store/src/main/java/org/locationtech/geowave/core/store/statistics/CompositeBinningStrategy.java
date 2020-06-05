package org.locationtech.geowave.core.store.statistics;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.index.persist.Persistable;
import org.locationtech.geowave.core.index.persist.PersistenceUtils;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.StatisticBinningStrategy;
import org.locationtech.geowave.core.store.entities.GeoWaveRow;
import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;

public class CompositeBinningStrategy implements StatisticBinningStrategy {

  public static final byte[] WILDCARD_BYTES = new byte[0];

  private StatisticBinningStrategy left;
  private StatisticBinningStrategy right;

  public CompositeBinningStrategy() {
    this.left = null;
    this.right = null;
  }

  public CompositeBinningStrategy(
      final StatisticBinningStrategy left,
      final StatisticBinningStrategy right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public <T> ByteArray[] getBins(DataTypeAdapter<T> adapter, T entry, GeoWaveRow... rows) {
    ByteArray[] leftBins = left.getBins(adapter, entry, rows);
    ByteArray[] rightBins = right.getBins(adapter, entry, rows);
    ByteArray[] bins = new ByteArray[leftBins.length * rightBins.length];
    int binIndex = 0;
    for (ByteArray leftBin : leftBins) {
      for (ByteArray rightBin : rightBins) {
        bins[binIndex++] = getBin(leftBin, rightBin);
      }
    }
    return bins;
  }

  @Override
  public byte[] toBinary() {
    return PersistenceUtils.toBinary(Lists.newArrayList(left, right));
  }

  @Override
  public void fromBinary(byte[] bytes) {
    List<Persistable> strategies = PersistenceUtils.fromBinaryAsList(bytes);
    if (strategies.size() == 2) {
      left = (StatisticBinningStrategy) strategies.get(0);
      right = (StatisticBinningStrategy) strategies.get(1);
    }
  }

  private int numStrategies() {
    int leftCount = 1;
    if (left instanceof CompositeBinningStrategy) {
      leftCount = ((CompositeBinningStrategy) left).numStrategies();
    }
    int rightCount = 1;
    if (right instanceof CompositeBinningStrategy) {
      rightCount = ((CompositeBinningStrategy) right).numStrategies();
    }
    return leftCount + rightCount;
  }

  public boolean usesStrategy(Class<? extends StatisticBinningStrategy> binningStrategyClass) {
    return binningStrategyClass.isAssignableFrom(left.getClass())
        || binningStrategyClass.isAssignableFrom(right.getClass())
        || (left instanceof CompositeBinningStrategy
            && ((CompositeBinningStrategy) left).usesStrategy(binningStrategyClass))
        || (right instanceof CompositeBinningStrategy
            && ((CompositeBinningStrategy) right).usesStrategy(binningStrategyClass));
  }

  public int getStrategyIndex(Class<? extends StatisticBinningStrategy> binningStrategyClass) {
    if (binningStrategyClass.isAssignableFrom(left.getClass())) {
      return 0;
    } else if (left instanceof CompositeBinningStrategy
        && ((CompositeBinningStrategy) left).usesStrategy(binningStrategyClass)) {
      return ((CompositeBinningStrategy) left).getStrategyIndex(binningStrategyClass);
    }
    int leftSize = 1;
    if (left instanceof CompositeBinningStrategy) {
      leftSize = ((CompositeBinningStrategy) left).numStrategies();
    }
    if (binningStrategyClass.isAssignableFrom(right.getClass())) {
      return leftSize;
    } else if (right instanceof CompositeBinningStrategy
        && ((CompositeBinningStrategy) right).usesStrategy(binningStrategyClass)) {
      return leftSize + ((CompositeBinningStrategy) right).getStrategyIndex(binningStrategyClass);
    }
    return -1;
  }

  public boolean isOfType(
      Class<? extends StatisticBinningStrategy> leftStrategy,
      Class<? extends StatisticBinningStrategy> rightStrategy) {
    return leftStrategy.isAssignableFrom(left.getClass())
        && rightStrategy.isAssignableFrom(right.getClass());
  }

  public static ByteArray getBin(ByteArray left, ByteArray right) {
    return new ByteArray(
        Bytes.concat(left.getBytes(), StatisticId.UNIQUE_ID_SEPARATOR, right.getBytes()));
  }

  public static boolean tokenMatches(ByteArray bin, int tokenIndex, ByteArray token) {
    int currentToken = 0;
    byte[] binBytes = bin.getBytes();
    int tokenStartIndex = -1;
    for (int i = 0; i < binBytes.length; i++) {
      if (currentToken == tokenIndex) {
        tokenStartIndex = i;
        break;
      }
      if (binBytes[i] == StatisticId.UNIQUE_ID_SEPARATOR[0]) {
        currentToken++;
      }
    }
    if (tokenStartIndex == -1 || token.getBytes().length + tokenStartIndex > binBytes.length) {
      return false;
    }
    byte[] tokenBytes = token.getBytes();
    for (int i = 0; i < tokenBytes.length; i++) {
      if (tokenBytes[i] != binBytes[i + tokenStartIndex]) {
        return false;
      }
    }
    if (tokenBytes.length + tokenStartIndex < binBytes.length
        && binBytes[tokenBytes.length + tokenStartIndex] != StatisticId.UNIQUE_ID_SEPARATOR[0]) {
      return false;
    }
    return true;
  }
}
