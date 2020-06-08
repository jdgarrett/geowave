package org.locationtech.geowave.core.store.metadata;

import java.util.Arrays;
import org.locationtech.geowave.core.index.ByteArrayUtils;
import org.locationtech.geowave.core.store.CloseableIterator;
import org.locationtech.geowave.core.store.entities.GeoWaveMetadata;
import org.locationtech.geowave.core.store.operations.MetadataQuery;

public class ClientMetadataFilteringIterator implements CloseableIterator<GeoWaveMetadata> {

  private final CloseableIterator<GeoWaveMetadata> source;
  private final MetadataQuery query;
  private final boolean hasSecondaryId;

  private GeoWaveMetadata next = null;

  public ClientMetadataFilteringIterator(
      final CloseableIterator<GeoWaveMetadata> source,
      final MetadataQuery query) {
    this.source = source;
    this.query = query;
    this.hasSecondaryId = query.getSecondaryId() != null;
  }

  private boolean secondaryIdMatches(final GeoWaveMetadata metadata) {
    return !hasSecondaryId || Arrays.equals(metadata.getSecondaryId(), query.getSecondaryId());
  }

  private boolean passesExactFilter(final GeoWaveMetadata metadata) {
    return (!query.hasPrimaryId() || Arrays.equals(metadata.getPrimaryId(), query.getPrimaryId()))
        && secondaryIdMatches(metadata);
  }

  private boolean passesPrefixFilter(final GeoWaveMetadata metadata) {
    return (!query.hasPrimaryId()
        || ByteArrayUtils.startsWith(metadata.getPrimaryId(), query.getPrimaryId()))
        && secondaryIdMatches(metadata);
  }

  private void computeNext() {
    while (next == null && source.hasNext()) {
      GeoWaveMetadata possibleNext = source.next();
      if (query.isPrefix()) {
        if (passesPrefixFilter(possibleNext)) {
          next = possibleNext;
        }
      } else if (passesExactFilter(possibleNext)) {
        next = possibleNext;
      }
    }
  }

  @Override
  public boolean hasNext() {
    if (next == null) {
      computeNext();
    }
    return next != null;
  }

  @Override
  public GeoWaveMetadata next() {
    if (next == null) {
      computeNext();
    }
    GeoWaveMetadata retVal = next;
    next = null;
    return retVal;
  }

  @Override
  public void close() {
    source.close();
  }

}
