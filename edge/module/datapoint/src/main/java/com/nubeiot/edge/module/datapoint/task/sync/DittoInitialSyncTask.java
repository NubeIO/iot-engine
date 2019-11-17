package com.nubeiot.edge.module.datapoint.task.sync;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.service.task.EntityTaskData;
import com.nubeiot.edge.module.datapoint.model.ditto.DittoDevice;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

import lombok.NonNull;

final class DittoInitialSyncTask extends AbstractDittoTask<Device>
    implements SyncTask.InitialSyncTask<DittoTaskContext> {

    DittoInitialSyncTask(DittoTaskContext context) {
        super(context);
    }

    @Override
    public @NonNull Single<Boolean> isExecutable(@NonNull EntityTaskData<Device> executionData) {
        return Single.just(true);
    }

    @Override
    public @NonNull Maybe<JsonObject> execute(@NonNull EntityTaskData<Device> executionData) {
        final DittoDevice ditto = new DittoDevice(executionData.getData());
        return doSyncOnSuccess(executionData.getMetadata(), ditto.creationEndpoint(thingId(definition())),
                               createReqBody(ditto), executionData.getData());
    }

    private JsonObject createReqBody(@NonNull DittoDevice pojo) {
        return new JsonObject().put("attributes", new JsonObject().put("extra", pojo.get().toJson()))
                               .put("features", new JsonObject().put("networks", new JsonObject())
                                                                .put("points", new JsonObject())
                                                                .put("histories", new JsonObject())
                                                                .put("realtime", new JsonObject())
                                                                .put("equipments", new JsonObject())
                                                                .put("transducers", new JsonObject()));
    }

}
