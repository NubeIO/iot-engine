package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.query.GroupQueryExecutor;

import lombok.NonNull;

public interface GroupEntityService<P extends VertxPojo, M extends EntityMetadata, CP extends CompositePojo<P, CP>,
                                       CM extends CompositeMetadata>
    extends BaseEntityService<M> {

    @NonNull CM contextGroup();

    @SuppressWarnings("unchecked")
    default GroupQueryExecutor<P, CP> groupQuery() {
        return GroupQueryExecutor.create(entityHandler(), context(), contextGroup());
    }

}
