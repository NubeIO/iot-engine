package com.nubeiot.core.workflow;

import io.reactivex.Maybe;
import io.reactivex.Single;

import lombok.NonNull;

/**
 * Represents {@code Task}.
 *
 * @param <DC> Type of {@code TaskDefinitionContext}
 * @param <EC> Type of {@code TaskExecutionContext}
 * @param <R>  Type of {@code Result}
 * @see TaskExecuter
 * @since 1.0.0
 */
public interface Task<DC extends TaskDefinitionContext, EC extends TaskExecutionContext, R> {

    /**
     * Defines task definition context.
     *
     * @return TaskDefinitionContext
     * @since 1.0.0
     */
    @NonNull DC definitionContext();

    /**
     * Check task is executable or not depends on runtime execution context
     *
     * @param runtimeContext the execution context
     * @return {@code True single} if executable or else {@code False single}
     * @since 1.0.0
     */
    @NonNull Single<Boolean> isExecutable(@NonNull EC runtimeContext);

    /**
     * Do execute task.
     *
     * @param runtimeContext the execution context
     * @return {@code result in Maybe}
     * @since 1.0.0
     */
    @NonNull Maybe<R> execute(@NonNull EC runtimeContext);

}
