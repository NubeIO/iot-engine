package com.nubeiot.core.sql.workflow;

import java.util.function.Function;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.transaction.JDBCRXTransactionExecutor;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.sql.workflow.step.DMLStep;
import com.nubeiot.core.sql.workflow.task.EntityTaskManager;

import lombok.Builder;
import lombok.NonNull;

@Builder
public final class DefaultDMLTransactionWorkflow implements DMLTransactionWorkflow {

    @NonNull
    private final DMLWorkflow workflow;

    @Override
    public @NonNull EventAction action() {
        return workflow.action();
    }

    @Override
    public @NonNull EntityMetadata metadata() {
        return workflow.metadata();
    }

    @Override
    public @NonNull Function<RequestData, RequestData> normalize() {
        return workflow.normalize();
    }

    @Override
    public @NonNull OperationValidator validator() {
        return workflow.validator();
    }

    @Override
    public @NonNull EntityTaskManager taskManager() {
        return workflow.taskManager();
    }

    @Override
    public @NonNull Single<JsonObject> run(@NonNull RequestData reqData) {
        return JDBCRXTransactionExecutor.create(sqlStep().queryExecutor().entityHandler().dsl())
                                        .transactionResult(c -> ((AbstractSQLWorkflow) workflow).run(reqData, c));
    }

    @Override
    public @NonNull DMLStep sqlStep() {
        return workflow.sqlStep();
    }

}
