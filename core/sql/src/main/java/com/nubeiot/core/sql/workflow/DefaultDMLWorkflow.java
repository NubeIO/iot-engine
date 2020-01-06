package com.nubeiot.core.sql.workflow;

import java.util.function.BiFunction;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.pojos.DMLPojo;
import com.nubeiot.core.sql.workflow.step.DMLStep;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
public final class DefaultDMLWorkflow extends AbstractSQLWorkflow implements DMLWorkflow {

    @NonNull
    private final DMLStep sqlStep;
    @NonNull
    private final BiFunction<RequestData, DMLPojo, Single<JsonObject>> transformer;

    @Override
    public Single<JsonObject> run(@NonNull RequestData requestData) {
        final RequestData reqData = normalize().apply(requestData);
        return sqlStep().execute(reqData, validator().andThen(afterValidation()))
                        .flatMap(dmlPojo -> postExecuter().execute(initSuccessData(reqData, dmlPojo.dbEntity()))
                                                          .map(pojo -> DMLPojo.clone(dmlPojo, pojo))
                                                          .switchIfEmpty(Single.just(dmlPojo)))
                        .doOnSuccess(db -> asyncPostExecuter().execute(initSuccessData(reqData, db.dbEntity())))
                        .doOnError(err -> asyncPostExecuter().execute(initErrorData(reqData, err)))
                        .flatMap(pojo -> transformer().apply(reqData, pojo));
    }

}
