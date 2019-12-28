package com.nubeiot.core.sql.workflow.task;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.workflow.Task;

/**
 * Represents Entity Task
 *
 * @param <DC> Type of {@code EntityTaskContext}
 * @param <P>  Type of {@code VertxPojo}
 * @param <R>  Type of {@code result}
 * @see Task
 * @see EntityTaskContext
 * @see EntityTaskData
 * @since 1.0.0
 */
public interface EntityTask<DC extends EntityTaskContext, P extends VertxPojo, R> extends Task<DC, EntityTaskData<P>, R> {

}
