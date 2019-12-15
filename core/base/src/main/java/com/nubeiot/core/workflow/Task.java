package com.nubeiot.core.workflow;

import io.reactivex.Maybe;
import io.reactivex.Single;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface Task<DC extends TaskDefinitionContext, EC extends TaskExecutionData, R> {

    @NonNull DC definition();

    @NonNull Single<Boolean> isExecutable(@NonNull EC executionData);

    @NonNull Maybe<R> execute(@NonNull EC executionData);

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
