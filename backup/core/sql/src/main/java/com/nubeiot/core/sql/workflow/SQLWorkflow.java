package com.nubeiot.core.sql.workflow;

import java.util.function.Function;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.sql.workflow.step.SQLStep;
import com.nubeiot.core.sql.workflow.task.EntityTaskManager;
import com.nubeiot.core.workflow.Workflow;

import lombok.NonNull;

/**
 * Represents workflow to query or manipulate entity data
 *
 * @since 1.0.0
 */
public interface SQLWorkflow extends Workflow {

    /**
     * Declares action.
     *
     * @return the event action
     * @since 1.0.0
     */
    @NonNull EventAction action();

    /**
     * Declares entity metadata.
     *
     * @return the entity metadata
     * @since 1.0.0
     */
    @NonNull EntityMetadata metadata();

    /**
     * Declares normalize {@code request data} function.
     *
     * @return the function
     * @since 1.0.0
     */
    @NonNull Function<RequestData, RequestData> normalize();

    /**
     * Declares {@code operation validator}.
     *
     * @return the operation validator
     * @see OperationValidator
     * @since 1.0.0
     */
    @NonNull OperationValidator validator();

    /**
     * Declares {@code Task manager}.
     *
     * @return the task manager
     * @see EntityTaskManager
     * @since 1.0.0
     */
    @NonNull EntityTaskManager taskManager();

    /**
     * Declares {@code SQL step} for interacting with database.
     *
     * @return the sql step
     * @see SQLStep
     * @since 1.0.0
     */
    @NonNull SQLStep sqlStep();

    /**
     * Kick off workflow.
     *
     * @param reqData the req data
     * @return json result in single
     * @since 1.0.0
     */
    @NonNull Single<JsonObject> run(@NonNull RequestData reqData);

}
