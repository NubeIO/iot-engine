package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.query.SimpleQueryExecutor;

import lombok.NonNull;

interface SimpleEntityService<P extends VertxPojo, M extends EntityMetadata>
    extends EntityService<P, M>, RequestDecorator {

    @Override
    @NonNull SimpleQueryExecutor<P> queryExecutor();

    default Single<? extends VertxPojo> lookupByKey(@NonNull Object key) {
        return queryExecutor().lookupByPrimaryKey(key);
    }

}
