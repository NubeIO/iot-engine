package com.nubeiot.edge.module.datapoint.sync;

import com.nubeiot.core.sql.service.task.EntityTask;
import com.nubeiot.core.sql.service.task.EntityTaskContext;
import com.nubeiot.core.sql.service.task.EntityTaskData;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

public interface InitialSyncTask<T extends EntityTaskContext> extends EntityTask<T, EntityTaskData<Device>> {

}
