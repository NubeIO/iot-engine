package com.nubeiot.core.workflow;

import io.reactivex.Maybe;
import io.reactivex.Single;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents for Task delegate.
 *
 * @param <DC> Type of {@code TaskDefinitionContext}
 * @param <EC> Type of {@code TaskExecutionData}
 * @param <R>  Type of {@code Result}
 * @since 1.0.0
 */
@RequiredArgsConstructor
public abstract class TaskDelegate<DC extends TaskDefinitionContext, EC extends TaskExecutionContext, R>
    implements Task<DC, EC, R> {

    @NonNull
    @Getter
    private final Task<DC, EC, R> delegate;

    @Override
    public @NonNull DC definitionContext() {
        return delegate.definitionContext();
    }

    @Override
    public @NonNull Single<Boolean> isExecutable(EC runtimeContext) {
        return delegate.isExecutable(runtimeContext);
    }

    @Override
    public @NonNull Maybe<R> execute(EC runtimeContext) {
        return delegate.execute(runtimeContext);
    }

}
