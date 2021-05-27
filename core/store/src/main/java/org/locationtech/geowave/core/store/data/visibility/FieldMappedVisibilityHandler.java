package org.locationtech.geowave.core.store.data.visibility;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import org.locationtech.geowave.core.index.StringUtils;
import org.locationtech.geowave.core.index.VarintUtils;
import org.locationtech.geowave.core.store.api.DataTypeAdapter;
import org.locationtech.geowave.core.store.api.VisibilityHandler;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

public class FieldMappedVisibilityHandler implements VisibilityHandler {
  private Map<String, byte[]> fieldVisibilities;

  public FieldMappedVisibilityHandler() {}

  public FieldMappedVisibilityHandler(final Map<String, byte[]> fieldVisibilities) {
    this.fieldVisibilities = fieldVisibilities;
  }

  @Override
  public <T> byte[] getVisibility(
      final DataTypeAdapter<T> adapter,
      final T rowValue,
      final String fieldName) {
    if (fieldVisibilities.containsKey(fieldName)) {
      return fieldVisibilities.get(fieldName);
    }
    return null;
  }

  @Override
  public byte[] toBinary() {
    int byteLength = VarintUtils.unsignedIntByteLength(fieldVisibilities.size());
    final Queue<byte[]> keyByteQueue = Queues.newArrayBlockingQueue(fieldVisibilities.size());
    for (Entry<String, byte[]> entry : fieldVisibilities.entrySet()) {
      final byte[] keyBytes = StringUtils.stringToBinary(entry.getKey());
      keyByteQueue.add(keyBytes);
      byteLength += VarintUtils.unsignedIntByteLength(keyBytes.length);
      byteLength += keyBytes.length;
      byteLength += VarintUtils.unsignedIntByteLength(entry.getValue().length);
      byteLength += entry.getValue().length;
    }
    final ByteBuffer buffer = ByteBuffer.allocate(byteLength);
    VarintUtils.writeUnsignedInt(fieldVisibilities.size(), buffer);
    for (Entry<String, byte[]> entry : fieldVisibilities.entrySet()) {
      final byte[] keyBytes = keyByteQueue.poll();
      VarintUtils.writeUnsignedInt(keyBytes.length, buffer);
      buffer.put(keyBytes);
      VarintUtils.writeUnsignedInt(entry.getValue().length, buffer);
      buffer.put(entry.getValue());
    }
    return buffer.array();
  }

  @Override
  public void fromBinary(byte[] bytes) {
    final ByteBuffer buffer = ByteBuffer.wrap(bytes);
    final int size = VarintUtils.readUnsignedInt(buffer);
    fieldVisibilities = Maps.newHashMapWithExpectedSize(size);
    for (int i = 0; i < size; i++) {
      final byte[] keyBytes = new byte[VarintUtils.readUnsignedInt(buffer)];
      buffer.get(keyBytes);
      final String key = StringUtils.stringFromBinary(keyBytes);
      final byte[] valueBytes = new byte[VarintUtils.readUnsignedInt(buffer)];
      buffer.get(valueBytes);
      fieldVisibilities.put(key, valueBytes);
    }
  }
}
