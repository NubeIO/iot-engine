package com.nubeiot.core.sql.service.workflow;

import java.util.function.BiFunction;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.pojos.DMLPojo;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
public final class DefaultDMLWorkflow extends AbstractSQLWorkflow implements DMLWorkflow {

    @NonNull
    private final SQLStep.DMLStep persist;
    @NonNull
    private final BiFunction<RequestData, DMLPojo, Single<JsonObject>> transformer;

    @Override
    public Single<JsonObject> execute(@NonNull RequestData requestData) {
        final RequestData reqData = normalize().apply(requestData);
        return persist().execute(reqData, validator().andThen(this::executeBlockingTask))
                        .doOnSuccess(db -> postAsyncPersist().execute(success(reqData, db.dbEntity())))
                        .doOnError(err -> postAsyncPersist().execute(error(reqData, err)))
                        .flatMap(pojo -> transformer().apply(reqData, pojo));
    }

}
