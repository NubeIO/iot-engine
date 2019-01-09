package com.nubeiot.edge.connector.bonescript.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.edge.connector.bonescript.BoneScriptVerticle;
import com.nubeiot.edge.connector.bonescript.model.tables.pojos.TblDitto;

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

    @EventContractor(events = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getList(RequestData data) {
        return getDbValue().map(value -> value.getJsonObject("thing")
                                              .getJsonObject("features")
                                              .getJsonObject("points")
                                              .getJsonObject("properties"));
    }

    private Single<JsonObject> getDbValue() {
        return this.verticle.getEntityHandler()
                            .getTblDittoDao()
                            .findOneById(1)
                            .map(optional -> optional.map(TblDitto::getValue))
                            .map(o -> o.orElseThrow(() -> new NotFoundException("Value doesn't exist on DB!")))
                            .map(JsonObject::new);
    }

}
