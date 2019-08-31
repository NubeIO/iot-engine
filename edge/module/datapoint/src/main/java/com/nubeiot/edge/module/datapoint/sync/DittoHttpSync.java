package com.nubeiot.edge.module.datapoint.sync;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.service.EntityPostService;
import com.nubeiot.core.sql.service.EntityService;
import com.nubeiot.core.transport.Transporter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DittoHttpSync implements EntityPostService {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @NonNull
    private final Vertx vertx;
    @NonNull
    private final JsonObject syncConfig;

    @Override
    public final Transporter transporter() {
        return HttpClientDelegate.create(vertx, syncConfig);
    }

    @Override
    public void onSuccess(EntityService service, EventAction action, JsonObject data) {
        if (action == EventAction.GET_LIST || action == EventAction.GET_ONE) {
            return;
        }
    }

    @Override
    public void onError(@NonNull EntityService service, EventAction action, Throwable throwable) {

    }

}
