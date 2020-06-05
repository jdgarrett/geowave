package org.locationtech.geowave.core.store.api;

import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import org.locationtech.geowave.core.store.statistics.adapter.AdapterStatisticType;
import org.locationtech.geowave.core.store.statistics.field.FieldStatisticType;
import org.locationtech.geowave.core.store.statistics.index.IndexStatisticType;
import org.locationtech.geowave.core.store.statistics.query.AdapterStatisticQueryBuilder;
import org.locationtech.geowave.core.store.statistics.query.FieldStatisticQueryBuilder;
import org.locationtech.geowave.core.store.statistics.query.IndexStatisticQueryBuilder;

public interface StatisticQueryBuilder<V extends StatisticValue<R>, R, B extends StatisticQueryBuilder<V, R, B>> {

  public B tag(final String tag);

  public B internal();

  public B statisticType(final StatisticType<V> statisticType);

  public B addAuthorization(final String authorization);

  public B authorizations(final String[] authorizations);

  public B addBin(final ByteArray bin);

  public B bins(final ByteArray[] bins);

  public StatisticQuery<V, R> build();

  public static <V extends StatisticValue<R>, R> IndexStatisticQueryBuilder<V, R> newBuilder(
      IndexStatisticType<V> statisticType) {
    return new IndexStatisticQueryBuilder<>(statisticType);
  }

  public static <V extends StatisticValue<R>, R> AdapterStatisticQueryBuilder<V, R> newBuilder(
      AdapterStatisticType<V> statisticType) {
    return new AdapterStatisticQueryBuilder<>(statisticType);
  }

  public static <V extends StatisticValue<R>, R> FieldStatisticQueryBuilder<V, R> newBuilder(
      FieldStatisticType<V> statisticType) {
    return new FieldStatisticQueryBuilder<>(statisticType);
  }

}
