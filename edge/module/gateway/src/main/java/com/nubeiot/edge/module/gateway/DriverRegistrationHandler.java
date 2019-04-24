package com.nubeiot.edge.module.gateway;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DriverRegistrationHandler implements EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(DriverRegistrationHandler.class);
    private final Vertx vertx;

    @EventContractor(action = {EventAction.CREATE, EventAction.INIT}, returnType = Single.class)
    public Single<JsonObject> installModule(RequestData data) {
        return Single.just(new JsonObject());
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.CREATE, EventAction.REMOVE);
    }

}
