package com.nubeiot.core.workflow;

import java.util.Objects;

import io.reactivex.Maybe;

import lombok.NonNull;

public interface TaskWorkflow<T extends Task> extends Workflow {

    T task();

    interface BlockingTaskWorkflow<T extends Task, INPUT, OUTPUT> extends TaskWorkflow<T> {

        @SuppressWarnings("unchecked")
        default Maybe<OUTPUT> execute(@NonNull TaskExecutionData<INPUT> taskData) {
            if (Objects.isNull(task())) {
                return Maybe.empty();
            }
            return TaskExecuter.blockingExecute(task(), taskData);
        }

    }


    interface AsyncTaskWorkflow<T extends Task, INPUT> extends TaskWorkflow<T> {

        @SuppressWarnings("unchecked")
        default void execute(@NonNull TaskExecutionData<INPUT> taskData) {
            if (Objects.isNull(task())) {
                return;
            }
            TaskExecuter.asyncExecute(task(), taskData);
        }

    }

}
