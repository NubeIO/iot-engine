package com.nubeiot.core.sql.query;

import java.util.function.Predicate;

import io.github.jklingsporn.vertx.jooq.rx.RXQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.jdbc.JDBCQueryExecutor;
import io.reactivex.Single;

import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.service.HasReferenceResource;

import lombok.NonNull;

/**
 * For complex query with {@code join}, {@code group by}
 */
public interface ComplexQueryExecutor<CP extends CompositePojo>
    extends ReferenceQueryExecutor<CP>, JDBCQueryExecutor<Single<?>>, RXQueryExecutor {

    static ComplexQueryExecutor create(@NonNull EntityHandler handler) {
        return new ComplexDaoQueryExecutor(handler);
    }

    /**
     * Context metadata
     *
     * @param metadata context metadata
     * @return a reference to this, so the API can be used fluently
     */
    ComplexQueryExecutor from(@NonNull CompositeMetadata metadata);

    /**
     * Resource presenter
     *
     * @param resourceMetadata resource metadata
     * @return a reference to this, so the API can be used fluently
     */
    ComplexQueryExecutor with(@NonNull EntityMetadata resourceMetadata);

    /**
     * Context metadata
     *
     * @param contextMetadata context metadata
     * @return a reference to this, so the API can be used fluently
     */
    ComplexQueryExecutor context(@NonNull EntityMetadata contextMetadata);

    ComplexQueryExecutor references(@NonNull HasReferenceResource.EntityReferences references);

    ComplexQueryExecutor viewPredicate(@NonNull Predicate<EntityMetadata> predicate);

}
