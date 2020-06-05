package org.locationtech.geowave.core.store.statistics.query;

import java.util.Arrays;
import java.util.List;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.Statistic;
import org.locationtech.geowave.core.store.api.StatisticQuery;
import org.locationtech.geowave.core.store.api.StatisticQueryBuilder;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import com.clearspring.analytics.util.Lists;

@SuppressWarnings("unchecked")
public abstract class AbstractStatisticQueryBuilder<V extends StatisticValue<R>, R, B extends StatisticQueryBuilder<V, R, B>>
    implements
    StatisticQueryBuilder<V, R, B> {

  protected String tag = null;

  protected StatisticType<V> statisticType = null;

  protected List<String> authorizations = Lists.newArrayList();

  protected List<ByteArray> bins = Lists.newArrayList();

  @Override
  public B tag(final String tag) {
    this.tag = tag;
    return (B) this;
  }

  @Override
  public B internal() {
    this.tag = Statistic.INTERNAL_TAG;
    return (B) this;
  }

  @Override
  public B statisticType(final StatisticType<V> statisticType) {
    this.statisticType = statisticType;
    return (B) this;
  }

  @Override
  public B addAuthorization(final String authorization) {
    authorizations.add(authorization);
    return (B) this;
  }

  @Override
  public B authorizations(final String[] authorizations) {
    if (authorizations != null) {
      this.authorizations = Arrays.asList(authorizations);
    } else {
      this.authorizations.clear();
    }
    return (B) this;
  }

  @Override
  public B addBin(final ByteArray bin) {
    bins.add(bin);
    return (B) this;
  }

  @Override
  public B bins(final ByteArray[] bins) {
    if (bins != null) {
      this.bins = Arrays.asList(bins);
    } else {
      this.bins.clear();
    }
    return (B) this;
  }
}
