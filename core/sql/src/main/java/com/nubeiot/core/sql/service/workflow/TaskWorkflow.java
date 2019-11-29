package com.nubeiot.core.sql.service.workflow;

import java.util.Objects;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.sql.service.task.EntityTask;
import com.nubeiot.core.sql.service.task.EntityTaskData;
import com.nubeiot.core.workflow.TaskExecuter;

import lombok.NonNull;

public interface TaskWorkflow extends Workflow {

    EntityTask task();

    interface BlockingTaskWorkflow extends TaskWorkflow {

        static BlockingTaskWorkflow create(EntityTask task) {
            return () -> task;
        }

        @SuppressWarnings("unchecked")
        default Single<VertxPojo> execute(@NonNull EntityTaskData<VertxPojo> taskData) {
            if (Objects.isNull(task())) {
                return Single.just(taskData.getData());
            }
            return TaskExecuter.blockingExecute(task(), taskData)
                               .switchIfEmpty(Single.just(taskData.getData()))
                               .map(VertxPojo.class::cast);
        }

    }


    interface AsyncTaskWorkflow extends TaskWorkflow {

        static AsyncTaskWorkflow create(EntityTask task) {
            return () -> task;
        }

        @SuppressWarnings("unchecked")
        default void execute(EntityTaskData<VertxPojo> taskData) {
            if (Objects.isNull(task())) {
                return;
            }
            TaskExecuter.asyncExecute(task(), taskData);
        }

    }

}
