package org.locationtech.geowave.core.geotime.store.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.locationtech.geowave.core.index.persist.PersistenceUtils;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

public class BoundingBoxStatisticTest {

  @Test
  public void testBoundingBoxStatisticSerialization()
      throws NoSuchAuthorityCodeException, FactoryException {
    BoundingBoxStatistic expected = new BoundingBoxStatistic("testType", "testField");
    byte[] statBytes = PersistenceUtils.toBinary(expected);
    BoundingBoxStatistic actual = (BoundingBoxStatistic) PersistenceUtils.fromBinary(statBytes);
    assertEquals(expected.getTypeName(), actual.getTypeName());
    assertEquals(expected.getFieldName(), actual.getFieldName());
    assertNull(actual.getTransform());
    assertNull(actual.getBinningStrategy());

    CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:4326");
    CoordinateReferenceSystem destinationCrs = CRS.decode("EPSG:3857");
    MathTransform expectedTransform = CRS.findMathTransform(sourceCrs, destinationCrs);
    expected = new BoundingBoxStatistic("testType", "testField", sourceCrs, destinationCrs);
    statBytes = PersistenceUtils.toBinary(expected);
    actual = (BoundingBoxStatistic) PersistenceUtils.fromBinary(statBytes);
    assertEquals(expected.getTypeName(), actual.getTypeName());
    assertEquals(expected.getFieldName(), actual.getFieldName());
    assertEquals(expected.getSourceCrs(), actual.getSourceCrs());
    assertEquals(expected.getDestinationCrs(), actual.getDestinationCrs());
    assertEquals(expected.getTransform(), actual.getTransform());
    assertEquals(expectedTransform, actual.getTransform());
    assertNull(actual.getBinningStrategy());
  }

}
