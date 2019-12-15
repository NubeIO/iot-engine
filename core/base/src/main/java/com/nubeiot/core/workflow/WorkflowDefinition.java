package com.nubeiot.core.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.workflow.WorkflowExecutor.DeliveryEventExecutor;

import lombok.NonNull;

/**
 * Represents for workflow definition
 *
 * @param <A> Type of workflow action
 */
@JsonTypeInfo(use = Id.NAME, property = "type")
public interface WorkflowDefinition<A extends ServiceRecord, E extends WorkflowExecutor> extends JsonData {

    //    /**
    ////     * Define action run after the main {@link #step()} when workflow is executed
    ////     *
    ////     * @param <W> Type of Workflow definition
    ////     * @return pre-workflow definition
    ////     */
    ////    <W extends WorkflowDefinition> W preStep();

    //    /**
    //     * Workflow type
    //     *
    //     * @return workflow type
    //     */
    //    @JsonUnwrapped
    //    @JsonProperty(value = "type", required = true)
    //    WorkflowType type();

    /**
     * Define main action of workflow
     *
     * @return Workflow step
     */
    @JsonProperty(value = "step", required = true)
    @NonNull A task();

    @NonNull Class<E> executorClass();

    /**
     * Define action run after the main {@link #task()} when workflow is executed
     *
     * @param <W> Type of Workflow definition
     * @return post-workflow definition
     */
    @JsonProperty(value = "postStep", required = true)
    <W extends ServiceRecord> W postTask();

    /**
     * Define {@code main action} result will be passed to {@code post step} in {@code another worker} or {@code same
     * worker} with {@code main action}.
     * <ul>
     *     <li>If {@code post step} in {@code another worker}, it means the workflow result will not be blocked, and it
     *     is main step result. Then the workflow result will be published to at least 2 addresses.</li>
     *     <li>If {@code post step} in {@code same worker}, it means the workflow result will be blocked to handle
     *     in {@code post step}. Then the workflow result will be converter result in {@code post step}</li>
     * </ul>
     *
     * @return {@code true} {@code post step} in {@code another worker}
     * @see #task()
     * @see #postTask()
     */
    boolean isConcurrent();

    /**
     * Define whether skipping {@code post step} if main action result raises error or not
     *
     * @return {@code true} if skipping error else otherwise
     */
    @JsonProperty(value = "skipInError")
    default boolean skipInError() {
        return false;
    }

    interface DeliveryEventDefinition extends WorkflowDefinition<EventbusServiceRecord, DeliveryEventExecutor> {

        @Override
        default @NonNull Class<DeliveryEventExecutor> executorClass() {
            return DeliveryEventExecutor.class;
        }

    }

}
