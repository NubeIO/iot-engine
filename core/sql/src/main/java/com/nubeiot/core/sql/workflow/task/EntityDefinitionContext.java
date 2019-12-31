package com.nubeiot.core.sql.workflow.task;

import java.util.function.Function;

import io.vertx.core.Vertx;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.workflow.TaskDefinitionContext;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents Entity task definition context.
 *
 * @see TaskDefinitionContext
 * @since 1.0.0
 */
public interface EntityDefinitionContext extends TaskDefinitionContext {

    /**
     * Create entity definition context.
     *
     * @param entityHandler the entity handler
     * @return the entity definition context
     * @since 1.0.0
     */
    static EntityDefinitionContext create(@NonNull EntityHandler entityHandler) {
        return new DefaultEntityDefinitionContext(entityHandler);
    }

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
    default TaskDefinitionContext registerSharedData(@NonNull Function<String, Object> sharedDataFunc) {
        return this;
    }

    /**
     * Default entity definition context.
     *
     * @since 1.0.0
     */
    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    class DefaultEntityDefinitionContext implements EntityDefinitionContext {

        private final EntityHandler entityHandler;

    }

}
