package com.nubeiot.core.sql.workflow.task;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.workflow.Task;

/**
 * Represents Entity Task
 *
 * @param <DC> Type of {@code EntityTaskContext}
 * @param <P>  Type of {@code VertxPojo}
 * @param <R>  Type of {@code Result}
 * @see Task
 * @see EntityDefinitionContext
 * @see EntityRuntimeContext
 * @since 1.0.0
 */
public interface EntityTask<DC extends EntityDefinitionContext, P extends VertxPojo, RC extends EntityRuntimeContext<P>
                               , R> extends Task<DC, RC, R> {

    interface EntityNormalTask<DC extends EntityDefinitionContext, P extends VertxPojo, R>
        extends EntityTask<DC, P, EntityRuntimeContext<P>, R> {

    }


    interface EntityPurgeTask<DC extends EntityDefinitionContext, P extends VertxPojo, R>
        extends EntityTask<DC, P, EntityRuntimePurgeContext<P>, R> {

    }

}
