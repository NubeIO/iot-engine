package com.nubeiot.core.sql.workflow;

import java.util.function.BiFunction;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.pojos.DMLPojo;
import com.nubeiot.core.sql.workflow.step.DMLStep;
import com.nubeiot.core.sql.workflow.task.EntityTaskExecuter.AsyncEntityTaskExecuter;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
public class DefaultDMLWorkflow<T extends DMLStep> extends AbstractSQLWorkflow implements DMLWorkflow {

    @NonNull
    private final T sqlStep;
    @NonNull
    private final BiFunction<RequestData, DMLPojo, Single<JsonObject>> transformer;

    @Override
    protected @NonNull Single<JsonObject> run(@NonNull RequestData requestData, Configuration runtimeConfig) {
        final RequestData reqData = normalize().apply(requestData);
        final AsyncEntityTaskExecuter postAsyncExecuter = taskManager().postAsyncExecuter();
        return sqlStep().execute(reqData, validator().andThen(afterValidation()), runtimeConfig)
                        .flatMap(dmlPojo -> taskManager().postBlockingExecuter()
                                                         .execute(initSuccessData(reqData, dmlPojo.dbEntity()))
                                                         .map(pojo -> DMLPojo.clone(dmlPojo, pojo))
                                                         .switchIfEmpty(Single.just(dmlPojo)))
                        .doOnSuccess(db -> postAsyncExecuter.execute(initSuccessData(reqData, db.dbEntity())))
                        .doOnError(err -> postAsyncExecuter.execute(initErrorData(reqData, err)))
                        .flatMap(pojo -> transformer().apply(reqData, pojo));
    }

}
