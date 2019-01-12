package com.nubeiot.edge.connector.bonescript.handlers;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.FEATURES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.POINTS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PROPERTIES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.THING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.connector.bonescript.BoneScriptVerticle;
import com.nubeiot.edge.connector.bonescript.utils.DittoDBUtils;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NonNull;

public class PointsEventHandler implements EventHandler {

    private final BoneScriptVerticle verticle;
    @Getter
    private final List<EventAction> availableEvents;

    public PointsEventHandler(@NonNull BoneScriptVerticle verticle, @NonNull EventModel eventModel) {
        this.verticle = verticle;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(eventModel.getEvents()));
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getList(RequestData data) {
        return DittoDBUtils.getDittoData(this.verticle.getEntityHandler())
                           .map(value -> value.getJsonObject(THING)
                                              .getJsonObject(FEATURES)
                                              .getJsonObject(POINTS)
                                              .getJsonObject(PROPERTIES));
    }

}
