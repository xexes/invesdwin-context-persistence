package de.invesdwin.context.persistence.jpa.api.query;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.Query;

import com.querydsl.jpa.impl.JPAQuery;

import de.invesdwin.context.persistence.jpa.PersistenceProperties;
import de.invesdwin.context.persistence.jpa.api.query.internal.ConfigurableJpaQuery;
import de.invesdwin.context.persistence.jpa.api.query.internal.ConfigurableQueryDslQuery;

@NotThreadSafe
public class QueryConfig {

    private Boolean cacheable;
    private Integer firstResult;
    private Integer maxResults;

    public QueryConfig withCacheable(final Boolean cacheable) {
        this.cacheable = cacheable;
        return this;
    }

    public QueryConfig withFirstResult(final Integer firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    public QueryConfig withMaxResults(final Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public <T extends Query> T configure(final String persistenceUnitName, final T query) {
        configure(persistenceUnitName, new ConfigurableJpaQuery(query));
        return query;
    }

    public JPAQuery configure(final String persistenceUnitName, final JPAQuery query) {
        configure(persistenceUnitName, new ConfigurableQueryDslQuery(query));
        return query;
    }

    private void configure(final String persistenceUnitName, final IConfigurableQuery query) {
        if (cacheable != null) {
            PersistenceProperties.getPersistenceUnitContext(persistenceUnitName).setCacheable(query, cacheable);
        }
        if (firstResult != null) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != null) {
            query.setMaxResults(maxResults);
        }
    }

    public static <T extends Query> T configure(final String persistenceUnitName, final T query,
            final QueryConfig config) {
        if (config != null) {
            config.configure(persistenceUnitName, query);
        }
        return query;
    }

    public static JPAQuery configure(final String persistenceUnitname, final JPAQuery query, final QueryConfig config) {
        if (config != null) {
            config.configure(persistenceUnitname, query);
        }
        return query;
    }

}
