package com.nubeiot.core.sql.workflow.step;

import java.util.function.BiFunction;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.sql.workflow.step.SQLBatchStep.DQLBatchStep;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Accessors(fluent = true)
@SuppressWarnings("unchecked")
public final class GetManyStep extends AbstractSQLStep implements DQLBatchStep {

    @Getter
    @NonNull
    private BiFunction<VertxPojo, RequestData, Single<JsonObject>> onEach;

    @Override
    public Single<JsonArray> query(@NonNull RequestData reqData, @NonNull OperationValidator validator) {
        return queryExecutor().findMany(reqData)
                              .flatMapSingle(pojo -> onEach().apply((VertxPojo) pojo, reqData))
                              .collect(JsonArray::new, (array, obj) -> ((JsonArray) array).add(obj));
    }

}
