package com.nubeiot.core.sql.service.workflow;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.service.task.EntityTask;
import com.nubeiot.core.workflow.TaskWorkflow;

public interface EntityTaskWorkflow extends TaskWorkflow<EntityTask> {

    EntityTask task();

    interface BlockingEntityTaskWorkflow
        extends BlockingTaskWorkflow<EntityTask, VertxPojo, VertxPojo>, EntityTaskWorkflow {

        static BlockingEntityTaskWorkflow create(EntityTask task) {
            return () -> task;
        }

    }


    interface AsyncEntityTaskWorkflow extends AsyncTaskWorkflow<EntityTask, VertxPojo>, EntityTaskWorkflow {

        static AsyncEntityTaskWorkflow create(EntityTask task) {
            return () -> task;
        }

    }

}
