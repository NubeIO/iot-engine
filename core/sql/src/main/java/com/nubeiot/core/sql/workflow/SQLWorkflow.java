package com.nubeiot.core.sql.workflow;

import java.util.function.Function;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.sql.workflow.EntityTaskExecuter.AsyncEntityTaskExecuter;
import com.nubeiot.core.sql.workflow.EntityTaskExecuter.BlockingEntityTaskExecuter;
import com.nubeiot.core.sql.workflow.step.SQLStep;
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
     * Declares {@code blocking pre-task} that is called after {@code validating} in {@link #validator()} and before
     * {@code executing SQL}.
     *
     * @return the pre blocking entity task workflow
     * @see BlockingEntityTaskExecuter
     * @since 1.0.0
     */
    @NonNull EntityTaskExecuter.BlockingEntityTaskExecuter preExecute();

    /**
     * Declares {@code SQL step} for interacting with database.
     *
     * @return the sql step
     * @see SQLStep
     * @since 1.0.0
     */
    @NonNull SQLStep sqlStep();

    /**
     * Declares the {@code blocking post-task} that is called after {@code executing SQL}.
     * <p>
     * {@code SQL Workflow} will not invoke {@code blocking post-task} if any error in {@code SQL execution} phase
     *
     * @return the post blocking entity task workflow
     * @see BlockingEntityTaskExecuter
     * @since 1.0.0
     */
    @NonNull EntityTaskExecuter.BlockingEntityTaskExecuter postExecute();

    /**
     * Declares {@code async post-task} that is called after {@code executing SQL}.
     * <p>
     * {@code SQL Workflow} will invoke {@code async post-task} regardless {@code SQL execution} phase is success or
     * error
     *
     * @return the async entity task workflow
     * @see AsyncEntityTaskExecuter
     * @since 1.0.0
     */
    @NonNull EntityTaskExecuter.AsyncEntityTaskExecuter asyncPostExecute();

    /**
     * Kick off workflow.
     *
     * @param reqData the req data
     * @return json result in single
     * @since 1.0.0
     */
    @NonNull Single<JsonObject> run(@NonNull RequestData reqData);

}
