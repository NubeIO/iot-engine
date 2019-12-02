package com.nubeiot.core.sql.service.workflow;

import java.util.function.BiFunction;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.service.workflow.SQLStep.DQLStep;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
@SuppressWarnings("unchecked")
public final class GetManyStep extends AbstractSQLStep implements DQLStep<JsonArray> {

    @NonNull
    private final BiFunction<VertxPojo, RequestData, Single<JsonObject>> transformFunction;

    @Override
    public Single<JsonArray> query(@NonNull RequestData reqData, @NonNull OperationValidator validator) {
        return queryExecutor().findMany(reqData)
                              .flatMapSingle(pojo -> transformFunction.apply((VertxPojo) pojo, reqData))
                              .collect(JsonArray::new, (array, obj) -> ((JsonArray) array).add(obj));
    }

}
