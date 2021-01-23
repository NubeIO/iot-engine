package com.nubeiot.core.workflow;

import io.reactivex.annotations.Experimental;
import io.vertx.core.Vertx;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.workflow.WorkflowDefinition.DeliveryEventDefinition;

import lombok.NonNull;

/**
 * Workflow executor
 */
@Experimental
public interface WorkflowExecutor<D extends WorkflowDefinition> {

    /**
     * Vertx
     *
     * @return vertx instance
     */
    @NonNull Vertx vertx();

    /**
     * Shared key to access shared data in current Vertx instance
     *
     * @return shared key
     */
    @NonNull String sharedKey();

    void init(@NonNull Vertx vertx, @NonNull String sharedKey);

    /**
     * Execute workflow
     *
     * @param definition workflow definition
     */
    void execute(@NonNull D definition);

    interface DeliveryEventExecutor extends WorkflowExecutor<DeliveryEventDefinition> {

        @Override
        default void execute(DeliveryEventDefinition definition) {
            final WorkflowDefinition postWorkflow = definition.postTask();
            final DeliveryEvent event = JsonData.from(definition.task().metadata(), DeliveryEvent.class);
            SharedDataDelegate.getEventController(vertx(), sharedKey()).fire(event);
        }

    }

}
