package com.nubeiot.core.workflow;

import io.reactivex.Maybe;
import io.reactivex.Single;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents for Task chain.
 *
 * @param <DC> Type of {@code TaskDefinitionContext}
 * @param <EC> Type of {@code TaskExecutionData}
 * @param <R>  Type of {@code Result}
 * @since 1.0.0
 */
@Getter
@Setter(value = AccessLevel.PROTECTED)
@Accessors(fluent = true)
public abstract class TaskChain<DC extends TaskDefinitionContext, EC extends TaskExecutionContext, R>
    implements Task<DC, EC, R> {

    private Task<DC, EC, R> chainedTask;

    @Override
    public abstract @NonNull DC definitionContext();

    @Override
    public abstract @NonNull Single<Boolean> isExecutable(EC runtimeContext);

    @Override
    public @NonNull Maybe<R> execute(EC runtimeContext) {
        return kickoff(runtimeContext).flatMap(r -> convertToChainContext(runtimeContext, r))
                                      .defaultIfEmpty(runtimeContext)
                                      .flatMap(this::nextExecute);
    }

    /**
     * Executes first task.
     *
     * @param executionContext the execution context
     * @return {@code result in Maybe}
     * @since 1.0.0
     */
    protected abstract @NonNull Maybe<R> kickoff(@NonNull EC executionContext);

    /**
     * Converts self result to next execution context for chaining call
     *
     * @param originExecutionContext the origin execution context
     * @param kickoffResult          the kickoff result
     * @return chain execution context
     * @since 1.0.0
     */
    protected abstract @NonNull Maybe<EC> convertToChainContext(@NonNull EC originExecutionContext, R kickoffResult);

    /**
     * Self executes task
     *
     * @param executionContext the execution context
     * @return result in {@code Maybe}
     * @since 1.0.0
     */
    protected abstract @NonNull Maybe<R> selfExecute(@NonNull EC executionContext);

    /**
     * Executes the chain task
     *
     * @param executionContext the execution context
     * @return chain result if chain task is executable or chain result is not empty. Otherwise, it is kickoff-result
     * @since 1.0.0
     */
    protected abstract @NonNull Maybe<R> nextExecute(@NonNull EC executionContext);

}
