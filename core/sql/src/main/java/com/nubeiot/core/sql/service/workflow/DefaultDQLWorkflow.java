package com.nubeiot.core.sql.service.workflow;

import java.util.function.BiFunction;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
public final class DefaultDQLWorkflow<T> extends AbstractSQLWorkflow implements DQLWorkflow<T> {

    @NonNull
    private final SQLStep.DQLStep<T> query;
    @NonNull
    private final BiFunction<RequestData, T, Single<JsonObject>> transformer;

    @Override
    public @NonNull Single<JsonObject> execute(@NonNull RequestData requestData) {
        final RequestData reqData = normalize().apply(requestData);
        return query().query(reqData, validator())
                      //                        .doOnSuccess(db -> postAsyncPersist().execute(success(reqData, db
                      //                        .pojo())))
                      //                        .doOnError(err -> postAsyncPersist().execute(error(reqData, err)))
                      .flatMap(pojo -> transformer().apply(reqData, pojo));
    }

}
