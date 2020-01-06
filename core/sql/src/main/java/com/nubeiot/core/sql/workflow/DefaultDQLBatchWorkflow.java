package com.nubeiot.core.sql.workflow;

import java.util.function.BiFunction;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.workflow.step.DQLBatchStep;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
public final class DefaultDQLBatchWorkflow extends AbstractSQLWorkflow implements DQLBatchWorkflow {

    @NonNull
    private final DQLBatchStep sqlStep;
    @NonNull
    private final BiFunction<RequestData, JsonArray, Single<JsonObject>> transformer;

    @Override
    public @NonNull Single<JsonObject> run(@NonNull RequestData requestData) {
        final RequestData reqData = normalize().apply(requestData);
        return sqlStep().query(reqData, validator().andThen(afterValidation()))
                        .flatMap(pojo -> transformer().apply(reqData, pojo));
    }

}
