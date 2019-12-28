package com.nubeiot.core.workflow;

import java.util.Objects;

import io.reactivex.Maybe;

import com.nubeiot.core.utils.ExecutorHelpers;

import lombok.NonNull;

/**
 * Represents for {@code Task} executer
 *
 * @param <T> Type of {@code Task}
 * @see Task
 * @since 1.0.0
 */
public interface TaskExecuter<T extends Task> {

    /**
     * Execute {@code Task} in {@code blocking} mode. The execution is run in {@code Worker Thread} or {@code EventLoop
     * Thread} depends on {@link TaskDefinitionContext#isConcurrent()}
     *
     * @param <TDC>         Type of {@code TaskDefinitionContext}
     * @param <TED>         Type of {@code TaskExecutionData}
     * @param <R>           Type of {@code Result}
     * @param <T>           Type of {@code Task}
     * @param task          the task
     * @param executionData the execution data
     * @return the maybe
     * @since 1.0.0
     */
    static <TDC extends TaskDefinitionContext, TED extends TaskExecutionData, R, T extends Task<TDC, TED, R>> Maybe<R> execute(
        T task, TED executionData) {
        final Maybe<R> execution = task.isExecutable(executionData)
                                       .filter(b -> b)
                                       .flatMap(b -> task.execute(executionData));
        if (task.definition().isConcurrent()) {
            return ExecutorHelpers.blocking(task.definition().vertx(), execution);
        }
        return execution;
    }

    /**
     * Defines {@code Task}.
     *
     * @return the task
     * @since 1.0.0
     */
    T task();

    /**
     * Represents for {@code task blocking-executer}.
     *
     * @param <T>      Type of {@code Task}
     * @param <INPUT>  Type of {@code INPUT}
     * @param <OUTPUT> Type of {@code OUTPUT}
     * @since 1.0.0
     */
    interface BlockingTaskExecuter<T extends Task, INPUT, OUTPUT> extends TaskExecuter<T> {

        /**
         * Execute task in blocking mode.
         *
         * @param taskData the task data
         * @return result in maybe
         * @see TaskExecuter#execute(Task, TaskExecutionData)
         * @since 1.0.0
         */
        @SuppressWarnings("unchecked")
        default Maybe<OUTPUT> execute(@NonNull TaskExecutionData<INPUT> taskData) {
            if (Objects.isNull(task())) {
                return Maybe.empty();
            }
            return TaskExecuter.execute(task(), taskData);
        }

    }


    /**
     * Represents for {@code task async-executer}.
     *
     * @param <T>     Type of {@code Task}
     * @param <INPUT> Type of {@code INPUT}
     * @since 1.0.0
     */
    interface AsyncTaskExecuter<T extends Task, INPUT> extends TaskExecuter<T> {

        /**
         * Execute task in async mode.
         *
         * @param taskData the task data
         * @since 1.0.0
         */
        @SuppressWarnings("unchecked")
        default void execute(@NonNull TaskExecutionData<INPUT> taskData) {
            if (Objects.isNull(task())) {
                return;
            }
            TaskExecuter.execute(task(), taskData).subscribe();
        }

    }

}
