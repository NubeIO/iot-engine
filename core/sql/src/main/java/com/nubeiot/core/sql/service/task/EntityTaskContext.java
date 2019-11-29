package com.nubeiot.core.sql.service.task;

import java.util.function.Function;

import io.vertx.core.Vertx;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.transport.Transporter;
import com.nubeiot.core.workflow.TaskDefinitionContext;

import lombok.NonNull;

public interface EntityTaskContext<T extends Transporter> extends TaskDefinitionContext<T> {

    @NonNull EntityHandler handler();

    @Override
    default @NonNull Vertx vertx() {
        return handler().vertx();
    }

    @Override
    default boolean isConcurrent() {
        return true;
    }

    @Override
    default <D> D getSharedDataValue(String dataKey) {
        return handler().sharedData(dataKey);
    }

    @Override
    default TaskDefinitionContext<T> registerSharedData(@NonNull Function<String, Object> sharedDataFunc) {
        return this;
    }

}
