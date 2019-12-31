package com.nubeiot.edge.module.datapoint.sync;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.workflow.task.ProxyEntityTask;
import com.nubeiot.core.transport.Transporter;

public interface SyncTask<DC extends SyncDefinitionContext, P extends VertxPojo, T extends Transporter>
    extends ProxyEntityTask<DC, P, JsonObject, T> {}
