package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.ReferenceEntityTransformer;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;

import lombok.NonNull;

/**
 * Represents service that holds a {@code resource} contains {@code reference} to another resources.
 * <p>
 * It means the {@code service context resource} is in {@code one-to-one} or {@code one-to-many} relationship to another
 * resource. In mapping to database layer, resource table has reference key to another table
 *
 * @param <P> Pojo type
 * @param <M> Metadata Type
 */
public interface OneToManyReferenceEntityService<P extends VertxPojo, M extends EntityMetadata>
    extends SimpleEntityService<P, M> {

    @Override
    @SuppressWarnings("unchecked")
    default @NonNull ReferenceQueryExecutor<P> queryExecutor() {
        return ReferenceQueryExecutor.create(entityHandler(), context());
    }

    @Override
    @NonNull ReferenceEntityTransformer transformer();

}
