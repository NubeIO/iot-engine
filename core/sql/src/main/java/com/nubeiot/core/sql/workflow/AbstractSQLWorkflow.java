package com.nubeiot.core.sql.workflow;

import java.util.function.Function;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.sql.workflow.task.EntityTaskData;
import com.nubeiot.core.sql.workflow.task.EntityTaskExecuter;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
abstract class AbstractSQLWorkflow implements SQLWorkflow {

    @NonNull
    private final EventAction action;
    @NonNull
    private final EntityMetadata metadata;
    @NonNull
    private final Function<RequestData, RequestData> normalize;
    @NonNull
    private final OperationValidator validator;
    @NonNull
    private final EntityTaskExecuter.BlockingEntityTaskExecuter preExecuter;
    @NonNull
    private final EntityTaskExecuter.BlockingEntityTaskExecuter postExecuter;
    @NonNull
    private final EntityTaskExecuter.AsyncEntityTaskExecuter asyncPostExecuter;

    Single<VertxPojo> afterValidation(@NonNull RequestData req, @NonNull VertxPojo pojo) {
        return preExecuter().execute(initSuccessData(req, pojo)).switchIfEmpty(Single.just(pojo));
    }

    EntityTaskData<VertxPojo> initSuccessData(@NonNull RequestData reqData, @NonNull VertxPojo pojo) {
        return taskData(reqData, pojo, null);
    }

    EntityTaskData<VertxPojo> initErrorData(@NonNull RequestData reqData, @NonNull Throwable err) {
        return taskData(reqData, null, err);
    }

    EntityTaskData<VertxPojo> taskData(@NonNull RequestData reqData, VertxPojo pojo, Throwable t) {
        return EntityTaskData.builder()
                             .originReqData(reqData)
                             .originReqAction(action())
                             .metadata(metadata())
                             .data(pojo)
                             .throwable(t)
                             .build();
    }

}
