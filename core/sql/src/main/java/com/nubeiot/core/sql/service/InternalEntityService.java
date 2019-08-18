package com.nubeiot.core.sql.service;

import java.util.function.BiFunction;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.query.SimpleQueryExecutor;
import com.nubeiot.core.sql.validation.EntityValidation;

import lombok.NonNull;

interface InternalEntityService<P extends VertxPojo, M extends EntityMetadata, V extends EntityValidation>
    extends EntityService<P, M, V> {

    @Override
    @NonNull SimpleQueryExecutor<P> queryExecutor();

    /**
     * Do recompute request data
     */
    @NonNull
    default RequestData recompute(@NonNull EventAction action, @NonNull RequestData requestData) {
        return requestData;
    }

    default Single<JsonObject> responseByLookupKey(@NonNull Object key, @NonNull RequestData reqData,
                                                   @NonNull BiFunction<VertxPojo, RequestData, JsonObject> handler) {
        final String keyName = metadata().requestKeyName();
        return transformer().response(keyName, key, k -> queryExecutor().lookupByPrimaryKey(k)
                                                                        .map(p -> handler.apply(p, reqData))
                                                                        .toSingle());
    }

}
