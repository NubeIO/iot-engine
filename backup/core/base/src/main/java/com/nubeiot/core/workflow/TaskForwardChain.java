package com.nubeiot.core.workflow;

import io.reactivex.Maybe;

import lombok.NonNull;

/**
 * Represents for Task forward chain.
 *
 * @param <DC> Type of {@code TaskDefinitionContext}
 * @param <EC> Type of {@code TaskExecutionData}
 * @param <R>  Type of {@code Result}
 * @since 1.0.0
 */
public abstract class TaskForwardChain<DC extends TaskDefinitionContext, EC extends TaskExecutionContext, R>
    extends TaskChain<DC, EC, R> {

    @Override
    protected @NonNull Maybe<R> kickoff(@NonNull EC executionContext) {
        return selfExecute(executionContext);
    }

    @Override
    protected @NonNull Maybe<R> nextExecute(@NonNull EC executionContext) {
        return TaskExecuter.execute(chainedTask(), executionContext);
    }

}
