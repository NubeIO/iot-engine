package com.nubeiot.core.sql.workflow.task;

import java.util.function.Function;

import io.vertx.core.Vertx;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.transport.Transporter;
import com.nubeiot.core.workflow.TaskDefinitionContext;

import lombok.NonNull;

/**
 * Represents Entity task definition context.
 *
 * @param <T> Type of {@code Transporter}
 * @see Transporter
 * @see TaskDefinitionContext
 * @since 1.0.0
 */
public interface EntityTaskContext<T extends Transporter> extends TaskDefinitionContext<T> {

    /**
     * Defines entity handler.
     *
     * @return the entity handler
     * @see EntityHandler
     * @since 1.0.0
     */
    @NonNull EntityHandler entityHandler();

    @Override
    default @NonNull Vertx vertx() {
        return entityHandler().vertx();
    }

    @Override
    default boolean isConcurrent() {
        return true;
    }

    @Override
    default <D> D getSharedDataValue(String dataKey) {
        return entityHandler().sharedData(dataKey);
    }

    @Override
    default TaskDefinitionContext<T> registerSharedData(@NonNull Function<String, Object> sharedDataFunc) {
        return this;
    }

}
