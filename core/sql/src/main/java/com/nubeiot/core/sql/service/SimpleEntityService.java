package com.nubeiot.core.sql.service;

import java.util.function.BiFunction;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.query.SimpleQueryExecutor;

import lombok.NonNull;

interface SimpleEntityService<P extends VertxPojo, M extends EntityMetadata>
    extends EntityService<P, M>, RequestDecorator {

    @Override
    @NonNull SimpleQueryExecutor<P> queryExecutor();

    default Single<JsonObject> responseByLookupKey(@NonNull Object key, @NonNull RequestData reqData,
                                                   @NonNull BiFunction<VertxPojo, RequestData, JsonObject> handler) {
        final String keyName = context().requestKeyName();
        return transformer().response(keyName, key,
                                      k -> queryExecutor().lookupByPrimaryKey(k).map(p -> handler.apply(p, reqData)));
    }

}
