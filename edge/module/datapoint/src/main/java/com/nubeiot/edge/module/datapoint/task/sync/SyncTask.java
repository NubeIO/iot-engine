package com.nubeiot.edge.module.datapoint.task.sync;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.service.task.EntityTask;
import com.nubeiot.core.sql.service.task.EntityTaskContext;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

public interface SyncTask<T extends EntityTaskContext, P extends VertxPojo> extends EntityTask<T, P, JsonObject> {

    interface InitialSyncTask<T extends EntityTaskContext> extends SyncTask<T, Device> {}

}
