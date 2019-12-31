package com.nubeiot.core.sql.workflow;

import java.util.function.BiFunction;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.workflow.step.DQLStep;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
public final class DefaultDQLWorkflow<T extends VertxPojo> extends AbstractSQLWorkflow implements DQLWorkflow<T> {

    @NonNull
    private final DQLStep<T> sqlStep;
    @NonNull
    private final BiFunction<RequestData, T, Single<JsonObject>> transformer;

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull Single<JsonObject> run(@NonNull RequestData requestData) {
        final RequestData reqData = normalize().apply(requestData);
        return sqlStep().query(reqData, validator().andThen(afterValidation()))
                        .flatMap(pojo -> postExecuter().execute(initSuccessData(reqData, pojo))
                                                       .switchIfEmpty(Single.just(pojo)))
                        .doOnSuccess(pojo -> asyncPostExecuter().execute(initSuccessData(reqData, pojo)))
                        .doOnError(err -> asyncPostExecuter().execute(initErrorData(reqData, err)))
                        .flatMap(pojo -> transformer().apply(reqData, (T) pojo));
    }

}
