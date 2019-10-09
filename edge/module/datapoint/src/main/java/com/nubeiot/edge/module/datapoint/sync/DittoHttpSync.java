package com.nubeiot.edge.module.datapoint.sync;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.auth.Credential;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.decorator.EntitySyncHandler;
import com.nubeiot.core.sql.service.EntityPostService;
import com.nubeiot.core.sql.service.EntityService;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex;

import lombok.NonNull;

public final class DittoHttpSync extends AbstractDittoHttpSync
    implements EntityPostService<HttpClientDelegate, IDittoModel<? extends VertxPojo>> {

    DittoHttpSync(Vertx vertx, JsonObject clientConfig, Credential credential) {
        super(vertx, clientConfig, credential);
    }

    @Override
    public IDittoModel<?> transform(@NonNull EntityService service, VertxPojo data) {
        return IDittoModel.create(service.context(), data.toJson());
    }

    @Override
    public void onSuccess(EntityService service, EventAction action, VertxPojo data, @NonNull RequestData requestData) {
        if (action == EventAction.GET_LIST || action == EventAction.GET_ONE ||
            !(service.entityHandler() instanceof EntitySyncHandler)) {
            return;
        }
        if (action == EventAction.REMOVE) {
            logger.error("Not yet supported sync with action = " + EventAction.REMOVE);
            return;
        }
        doSyncOnSuccess(service, action, transform(service, data), requestData).subscribe();
    }

    @Override
    public void onError(@NonNull EntityService service, EventAction action, Throwable throwable) {
        if (action == EventAction.GET_LIST || action == EventAction.GET_ONE) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.error("Not sync due to previous error", throwable);
        }
    }

    @Override
    public Maybe<JsonObject> doSyncOnSuccess(@NonNull EntityService service, @NonNull EventAction action,
                                             IDittoModel<?> syncData, @NonNull RequestData requestData) {
        final @NonNull EntitySyncHandler entityHandler = (EntitySyncHandler) service.entityHandler();
        final String thingId = Strings.format("com.nubeio.{0}:{1}",
                                              entityHandler.sharedData(DataPointIndex.CUSTOMER_CODE),
                                              UUID64.uuid64ToUuidStr(
                                                  entityHandler.sharedData(DataPointIndex.DEVICE_ID)));
        final RequestData reqData = RequestData.builder().headers(createRequestHeader()).body(syncData.body()).build();
        return doSyncOnSuccess(entityHandler, service.context(), syncData.endpoint(thingId), syncData.get(), reqData);
    }

}
