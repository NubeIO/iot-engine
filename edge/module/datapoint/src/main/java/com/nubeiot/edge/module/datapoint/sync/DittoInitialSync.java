package com.nubeiot.edge.module.datapoint.sync;

import io.reactivex.Maybe;
import io.vertx.core.json.JsonObject;

import com.nubeiot.auth.Credential;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.decorator.EntitySyncHandler;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.module.datapoint.model.ditto.DittoDevice;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.DeviceMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

import lombok.NonNull;

public final class DittoInitialSync extends AbstractDittoHttpSync implements InitialSync<HttpClientDelegate> {

    private final EntitySyncHandler syncHandler;

    DittoInitialSync(@NonNull EntitySyncHandler syncHandler, @NonNull JsonObject clientConfig, Credential credential) {
        super(syncHandler.vertx(), clientConfig, credential);
        this.syncHandler = syncHandler;
    }

    @Override
    public Maybe<JsonObject> sync(@NonNull Device device) {
        final String thingId = Strings.format("com.nubeio.{0}:{1}",
                                              syncHandler.sharedData(DataPointIndex.CUSTOMER_CODE),
                                              UUID64.uuid64ToUuidStr(syncHandler.sharedData(DataPointIndex.DEVICE_ID)));
        DittoDevice ditto = new DittoDevice(device);
        JsonObject body = new JsonObject().put("attributes", new JsonObject().put("extra", ditto.body()))
                                          .put("features", new JsonObject().put("networks", new JsonObject())
                                                                           .put("points", new JsonObject())
                                                                           .put("histories", new JsonObject())
                                                                           .put("realtime", new JsonObject())
                                                                           .put("equipments", new JsonObject())
                                                                           .put("transducers", new JsonObject()));
        return doSyncOnSuccess(syncHandler, DeviceMetadata.INSTANCE, ditto.creationEndpoint(thingId), device,
                               RequestData.builder().headers(createRequestHeader()).body(body).build());
    }

}
