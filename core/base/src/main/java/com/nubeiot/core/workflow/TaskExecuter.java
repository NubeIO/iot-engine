package com.nubeiot.core.workflow;

import io.reactivex.Maybe;

import com.nubeiot.core.utils.ExecutorHelpers;

public final class TaskExecuter {

    public static <TDC extends TaskDefinitionContext, TED extends TaskExecutionData, R, T extends Task<TDC, TED, R>> void asyncExecute(
        T task, TED executionData) {
        blockingExecute(task, executionData).subscribe();
    }

    public static <TDC extends TaskDefinitionContext, TED extends TaskExecutionData, R, T extends Task<TDC, TED, R>> Maybe<R> blockingExecute(
        T task, TED executionData) {
        final Maybe<R> execution = task.isExecutable(executionData)
                                       .filter(b -> b).flatMap(b -> task.execute(executionData));
        if (task.definition().isConcurrent()) {
            return ExecutorHelpers.blocking(task.definition().vertx(), execution);
        }
        return execution;
    }

}
