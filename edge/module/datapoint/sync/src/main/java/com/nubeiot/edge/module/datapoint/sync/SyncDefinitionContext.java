package com.nubeiot.edge.module.datapoint.sync;

import io.vertx.core.json.JsonObject;

import com.nubeiot.auth.Credential;
import com.nubeiot.core.sql.workflow.task.EntityDefinitionContext;

import lombok.NonNull;

public interface SyncDefinitionContext extends EntityDefinitionContext {

    @NonNull String type();

    @NonNull JsonObject transporterConfig();

    Credential credential();

}
