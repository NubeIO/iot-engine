package com.nubeiot.core.workflow;

import io.reactivex.Maybe;
import io.reactivex.Single;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
     * @param executionContext the execution context
     * @return {@code True single} if executable or else {@code False single}
     * @since 1.0.0
     */
    @NonNull Single<Boolean> isExecutable(@NonNull EC executionContext);

    /**
     * Do execute task.
     *
     * @param executionContext the execution context
     * @return {@code result in Maybe}
     * @since 1.0.0
     */
    @NonNull Maybe<R> execute(@NonNull EC executionContext);

    /**
     * Represents for Task delegate.
     *
     * @param <DC> Type of {@code TaskDefinitionContext}
     * @param <EC> Type of {@code TaskExecutionData}
     * @param <R>  Type of {@code Result}
     * @since 1.0.0
     */
    @RequiredArgsConstructor
    abstract class TaskDelegate<DC extends TaskDefinitionContext, EC extends TaskExecutionContext, R>
        implements Task<DC, EC, R> {

        @NonNull
        @Getter
        private final Task<DC, EC, R> delegate;

        @Override
        public @NonNull DC definitionContext() {
            return delegate.definitionContext();
        }

        @Override
        public @NonNull Single<Boolean> isExecutable(EC executionContext) {
            return delegate.isExecutable(executionContext);
        }

        @Override
        public @NonNull Maybe<R> execute(EC executionContext) {
            return delegate.execute(executionContext);
        }

    }

}
