package com.nubeiot.core.workflow;

import io.vertx.core.Vertx;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.transport.ProxyService;
import com.nubeiot.core.transport.Transporter;

import lombok.NonNull;

/**
 * Represents {@code Task definition context}.
 *
 * @param <T> Type of {@code Transporter}
 * @see Transporter
 * @see ProxyService
 * @see SharedDataDelegate
 * @since 1.0.0
 */
public interface TaskDefinitionContext<T extends Transporter>
    extends TaskContext, ProxyService<T>, SharedDataDelegate<TaskDefinitionContext<T>> {

    /**
     * Vertx
     *
     * @return vertx instance
     * @since 1.0.0
     */
    @NonNull Vertx vertx();

    /**
     * Define {@code task} will be executed in {@code another worker thread} or {@code same thread} with current
     * thread.
     *
     * @return {@code true} {@code task} will be executed in {@code another worker}
     * @since 1.0.0
     */
    boolean isConcurrent();

}
