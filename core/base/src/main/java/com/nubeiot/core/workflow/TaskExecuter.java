package com.nubeiot.core.workflow;

import java.util.Objects;

import io.reactivex.Maybe;

import com.nubeiot.core.utils.ExecutorHelpers;

public final class TaskExecuter {

    public static <DC extends TaskDefinitionContext, EC extends TaskExecutionData, R> void execute(
        Task<DC, EC, R> task, EC executionContext) {
        final Maybe<R> execution = task.isExecutable(executionContext)
                                       .filter(b -> b)
                                       .flatMap(b -> task.execute(executionContext));
        if (Objects.nonNull(task.definition()) && task.definition().isAsync()) {
            ExecutorHelpers.blocking(task.definition().vertx(), execution).subscribe();
            return;
        }
        execution.subscribe();
    }

}
