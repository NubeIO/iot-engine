package com.nubeiot.core.sql.service.task;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.workflow.Task;

public interface EntityTask<DC extends EntityTaskContext, P extends VertxPojo, R>
    extends Task<DC, EntityTaskData<P>, R> {

}
