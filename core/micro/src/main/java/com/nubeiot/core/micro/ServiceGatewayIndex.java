package com.nubeiot.core.micro;

import java.util.Arrays;
import java.util.Collection;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ServiceGatewayIndex implements EventListener {

    @NonNull
    private final MicroContext context;

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_ONE, EventAction.GET_LIST);
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(@NonNull RequestData requestData) {
        return Single.just(requestData.toJson());
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(@NonNull RequestData requestData) {
        return Single.just(requestData.toJson());
    }

}
