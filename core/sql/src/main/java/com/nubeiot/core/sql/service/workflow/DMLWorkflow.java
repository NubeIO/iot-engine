package com.nubeiot.core.sql.service.workflow;

import java.util.function.Function;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.workflow.TaskWorkflow.AsyncTaskWorkflow;
import com.nubeiot.core.sql.service.workflow.TaskWorkflow.BlockingTaskWorkflow;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.NonNull;

/**
 * DML workflow is used for adding (inserting), deleting, and modifying (updating) data in a database
 */
public interface DMLWorkflow extends Workflow {

    @NonNull EventAction action();

    @NonNull EntityMetadata metadata();

    @NonNull Function<RequestData, RequestData> normalize();

    @NonNull OperationValidator validator();

    BlockingTaskWorkflow prePersist();

    @NonNull PersistStep persist();

    AsyncTaskWorkflow postAsyncPersist();

    @NonNull Single<JsonObject> execute(@NonNull RequestData reqData);

}
