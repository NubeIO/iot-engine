package com.nubeiot.core.workflow;

import io.vertx.core.Vertx;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.transport.ProxyService;
import com.nubeiot.core.transport.Transporter;

import lombok.NonNull;

public interface TaskDefinitionContext<T extends Transporter>
    extends TaskContext, ProxyService<T>, SharedDataDelegate<TaskDefinitionContext<T>> {

    /**
     * Vertx
     *
     * @return vertx instance
     */
    @NonNull Vertx vertx();

    /**
     * Define {@code task} will be executed in {@code another worker} or {@code same worker} with current thread.
     *
     * @return {@code true} {@code task} will be executed in {@code another worker}
     */
    boolean isConcurrent();

}
