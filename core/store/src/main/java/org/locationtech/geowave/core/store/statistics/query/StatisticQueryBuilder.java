package org.locationtech.geowave.core.store.statistics.query;

import java.util.Arrays;
import java.util.List;
import org.locationtech.geowave.core.index.ByteArray;
import org.locationtech.geowave.core.store.api.StatisticValue;
import org.locationtech.geowave.core.store.statistics.StatisticType;
import com.clearspring.analytics.util.Lists;

@SuppressWarnings("unchecked")
public abstract class StatisticQueryBuilder<V extends StatisticValue<R>, R, B extends StatisticQueryBuilder<V, R, B>> {
  
  protected String statisticName = null;

  protected StatisticType<V> statisticType = null;
  
  protected List<String> authorizations = Lists.newArrayList();

  private List<ByteArray> bins = Lists.newArrayList();
  
  public B setName(final String name) {
    this.statisticName = name;
    return (B) this;
  }
  
  public B setStatisticType(final StatisticType<V> statisticType) {
    this.statisticType = statisticType;
    return (B) this;
  }
  
  public B addAuthorization(final String authorization) {
    authorizations.add(authorization);
    return (B) this;
  }

  public B setAuthorizations(final String[] authorizations) {
    if (authorizations != null) {
      this.authorizations = Arrays.asList(authorizations);
    } else {
      this.authorizations.clear();
    }
    return (B) this;
  }
  
  public B addBin(final ByteArray bin) {
    bins.add(bin);
    return (B) this;
  }
  
  public B setBins(final ByteArray[] bins) {
    if (bins != null) {
      this.bins = Arrays.asList(bins);
    } else {
      this.bins.clear();
    }
    return (B) this;
  }
  
  
  public abstract StatisticsQuery<V, R> build();

}
