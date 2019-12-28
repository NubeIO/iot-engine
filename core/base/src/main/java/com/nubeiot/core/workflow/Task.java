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
 * @param <EC> Type of {@code TaskExecutionData}
 * @param <R>  Type of {@code Result}
 * @see TaskExecuter
 * @since 1.0.0
 */
public interface Task<DC extends TaskDefinitionContext, EC extends TaskExecutionData, R> {

    /**
     * Defines TaskDefinitionContext.
     *
     * @return TaskDefinitionContext
     * @since 1.0.0
     */
    @NonNull DC definition();

    /**
     * Check task is executable or not depends on TaskExecutionData
     *
     * @param executionData the execution data
     * @return {@code True single} if executable or else {@code False single}
     * @since 1.0.0
     */
    @NonNull Single<Boolean> isExecutable(@NonNull EC executionData);

    /**
     * Do execute task.
     *
     * @param executionData the execution data
     * @return {@code result in Maybe}
     * @since 1.0.0
     */
    @NonNull Maybe<R> execute(@NonNull EC executionData);

    /**
     * Represents for Task delegate.
     *
     * @param <DC> Type of {@code TaskDefinitionContext}
     * @param <EC> Type of {@code TaskExecutionData}
     * @param <R>  Type of {@code Result}
     * @since 1.0.0
     */
    @RequiredArgsConstructor
    abstract class TaskDelegate<DC extends TaskDefinitionContext, EC extends TaskExecutionData, R>
        implements Task<DC, EC, R> {

        @NonNull
        @Getter
        private final Task<DC, EC, R> delegate;

        @Override
        public @NonNull DC definition() {
            return delegate.definition();
        }

        @Override
        public @NonNull Single<Boolean> isExecutable(EC executionData) {
            return delegate.isExecutable(executionData);
        }

        @Override
        public @NonNull Maybe<R> execute(EC executionData) {
            return delegate.execute(executionData);
        }

    }

}
