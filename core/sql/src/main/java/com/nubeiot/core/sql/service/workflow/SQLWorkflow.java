package com.nubeiot.core.sql.service.workflow;

import java.util.function.Function;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.workflow.EntityTaskWorkflow.AsyncEntityTaskWorkflow;
import com.nubeiot.core.sql.service.workflow.EntityTaskWorkflow.BlockingEntityTaskWorkflow;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.workflow.Workflow;

import lombok.NonNull;

/**
 * Represents workflow to query or manipulate entity data
 */
public interface SQLWorkflow extends Workflow {

    @NonNull EventAction action();

    @NonNull EntityMetadata metadata();

    @NonNull Function<RequestData, RequestData> normalize();

    @NonNull OperationValidator validator();

    BlockingEntityTaskWorkflow prePersist();

    AsyncEntityTaskWorkflow postAsyncPersist();

    @NonNull Single<JsonObject> execute(@NonNull RequestData reqData);

}
