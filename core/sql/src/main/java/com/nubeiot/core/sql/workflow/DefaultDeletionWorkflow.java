package com.nubeiot.core.sql.workflow;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.pojos.DMLPojo;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.sql.workflow.step.DeletionStep;
import com.nubeiot.core.sql.workflow.task.EntityTask.EntityPurgeTask;
import com.nubeiot.core.workflow.TaskExecuter;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
public final class DefaultDeletionWorkflow extends DefaultDMLWorkflow<DeletionStep> {

    private final boolean supportForceDeletion;

    @Override
    protected @NonNull OperationValidator afterValidation() {
        if (supportForceDeletion) {
            return super.afterValidation().andThen(OperationValidator.create(this::purgeTask));
        }
        return super.afterValidation();
    }

    private Single<VertxPojo> purgeTask(@NonNull RequestData reqData, @NonNull VertxPojo pojo) {
        return TaskExecuter.execute(EntityPurgeTask.create(sqlStep().queryExecutor()), initSuccessData(reqData, pojo))
                           .map(DMLPojo::request)
                           .toSingle(pojo);
    }

}
