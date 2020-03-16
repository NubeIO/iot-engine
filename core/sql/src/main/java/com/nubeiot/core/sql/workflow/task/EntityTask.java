package com.nubeiot.core.sql.workflow.task;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.sql.pojos.DMLPojo;
import com.nubeiot.core.sql.query.EntityQueryExecutor;
import com.nubeiot.core.sql.service.EntityApiService;
import com.nubeiot.core.workflow.Task;

import lombok.NonNull;

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
public interface EntityTask<DC extends EntityDefinitionContext, P extends VertxPojo, R>
    extends Task<DC, EntityRuntimeContext<P>, R> {

    interface EntityNormalTask<DC extends EntityDefinitionContext, P extends VertxPojo, R>
        extends EntityTask<DC, P, R> {

    }


    interface EntityPurgeTask<DC extends PurgeDefinitionContext, P extends VertxPojo, R>
        extends EntityTask<DC, P, R>, ProxyEntityTask<DC, P, R, EventbusClient> {

        static <P extends VertxPojo> EntityPurgeTask<PurgeDefinitionContext, P, DMLPojo> create(
            @NonNull EntityQueryExecutor queryExecutor) {
            return new DefaultEntityPurgeTask<>(PurgeDefinitionContext.create(queryExecutor));
        }

        @Override
        default EventbusClient transporter() {
            return definitionContext().entityHandler().eventClient();
        }

        @NonNull
        default EntityApiService apiService() {
            return definitionContext().entityHandler().sharedData(EntityApiService.DATA_KEY);
        }

    }

}
