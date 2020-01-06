package com.nubeiot.core.sql.workflow;

import java.util.function.Function;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.sql.workflow.task.EntityRuntimeContext;
import com.nubeiot.core.sql.workflow.task.EntityTask;
import com.nubeiot.core.sql.workflow.task.EntityTaskExecuter;
import com.nubeiot.core.sql.workflow.task.EntityTaskExecuter.AsyncEntityTaskExecuter;
import com.nubeiot.core.sql.workflow.task.EntityTaskExecuter.BlockingEntityTaskExecuter;

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

    @NonNull OperationValidator afterValidation() {
        return OperationValidator.create(
            (req, pojo) -> preExecuter().execute(initSuccessData(req, pojo)).switchIfEmpty(Single.just(pojo)));
    }

    @NonNull EntityRuntimeContext<VertxPojo> initSuccessData(@NonNull RequestData reqData, @NonNull VertxPojo pojo) {
        return taskData(reqData, pojo, null);
    }

    @NonNull EntityRuntimeContext<VertxPojo> initErrorData(@NonNull RequestData reqData, @NonNull Throwable err) {
        return taskData(reqData, null, err);
    }

    @NonNull EntityRuntimeContext<VertxPojo> taskData(@NonNull RequestData reqData, VertxPojo pojo, Throwable t) {
        return EntityRuntimeContext.builder()
                                   .originReqData(reqData)
                                   .originReqAction(action())
                                   .metadata(metadata())
                                   .data(pojo)
                                   .throwable(t)
                                   .build();
    }

    static abstract class AbstractSQLWorkflowBuilder<C extends AbstractSQLWorkflow,
                                                        B extends AbstractSQLWorkflowBuilder<C, B>> {

        public B preTask(EntityTask preTask) {
            this.preExecuter = BlockingEntityTaskExecuter.create(preTask);
            return self();
        }

        public B postTask(EntityTask postTask) {
            this.postExecuter = BlockingEntityTaskExecuter.create(postTask);
            return self();
        }

        public B asyncPostTask(EntityTask asyncPostTask) {
            this.asyncPostExecuter = AsyncEntityTaskExecuter.create(asyncPostTask);
            return self();
        }

    }

}
