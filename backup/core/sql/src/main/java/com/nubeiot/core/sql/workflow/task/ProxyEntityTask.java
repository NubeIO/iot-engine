package com.nubeiot.core.sql.workflow.task;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.transport.Transporter;
import com.nubeiot.core.workflow.ProxyTask;

public interface ProxyEntityTask<DC extends EntityDefinitionContext, P extends VertxPojo, R, T extends Transporter>
    extends EntityTask<DC, P, R>, ProxyTask<DC, EntityRuntimeContext<P>, R, T> {

}
