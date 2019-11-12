package com.nubeiot.core.workflow;

import io.reactivex.Maybe;
import io.reactivex.Single;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface Task<DC extends TaskDefinitionContext, EC extends TaskExecutionData, R> {

    DC definition();

    @NonNull Single<Boolean> isExecutable(@NonNull EC executionContext);

    @NonNull Maybe<R> execute(@NonNull EC executionContext);

    @RequiredArgsConstructor
    abstract class TaskDelegate<DC extends TaskDefinitionContext, EC extends TaskExecutionData, R>
        implements Task<DC, EC, R> {

        @NonNull
        @Getter
        private final Task<DC, EC, R> delegate;

        @Override
        public DC definition() {
            return delegate.definition();
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
