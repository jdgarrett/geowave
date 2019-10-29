package org.locationtech.geowave.adapter.vector.query.gwql;

import java.util.List;
import java.util.NoSuchElementException;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A result set that wraps a single result.
 */
public class SingletonResultSet implements ResultSet {

  private Result next;

  private final List<String> columnNames;
  private final List<Class<?>> columnTypes;

  /**
   * @param columnNames the display name of each column
   * @param columnTypes the type of each column
   * @param values the values of each column
   */
  public SingletonResultSet(
      final List<String> columnNames,
      final List<Class<?>> columnTypes,
      final List<Object> values) {
    this.columnNames = columnNames;
    this.columnTypes = columnTypes;
    next = new Result(values);
  }

  @Override
  public void close() {}

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public Result next() {
    if (next != null) {
      Result retVal = next;
      next = null;
      return retVal;
    }
    throw new NoSuchElementException();
  }

  @Override
  public int columnCount() {
    return columnNames.size();
  }

  @Override
  public String columnName(int index) {
    return columnNames.get(index);
  }

  @Override
  public Class<?> columnType(int index) {
    return columnTypes.get(index);
  }

  @Override
  public CoordinateReferenceSystem getCRS() {
    return DefaultGeographicCRS.WGS84;
  }

}