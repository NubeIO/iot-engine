package com.nubeiot.core.sql.service.task;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.workflow.Task;

public interface EntityTask<DC extends EntityTaskContext, EC extends EntityTaskData> extends Task<DC, EC, JsonObject> {

}
