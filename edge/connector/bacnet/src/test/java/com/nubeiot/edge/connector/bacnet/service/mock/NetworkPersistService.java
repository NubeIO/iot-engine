package com.nubeiot.edge.connector.bacnet.service.mock;

import java.util.Collection;
import java.util.Collections;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

public class NetworkPersistService implements EventListener {

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Collections.singleton(EventAction.CREATE);
    }

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData reqData) {
        return Single.just(JsonPojo.from(new Network().fromJson(reqData.body())).toJson());
    }

}
