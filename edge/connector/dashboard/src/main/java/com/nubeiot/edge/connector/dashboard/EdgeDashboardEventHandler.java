package com.nubeiot.edge.connector.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.connector.dashboard.model.tables.interfaces.ITblDashboardConnection;

import lombok.NonNull;
import lombok.Setter;

public class EdgeDashboardEventHandler implements EventHandler {

    @Setter
    EdgeDashboardEntityHandler entityHandler;

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> get(RequestData data) {
        return entityHandler.findDashboardConnectionRecords()
                            .flattenAsObservable(r -> r)
                            .flatMapSingle(r -> Single.just(r.toJson()))
                            .toList()
                            .map(r -> new JsonObject().put("records", r));
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData data) {
        JsonObject body = data.body();
        return entityHandler.findDashboardConnectionRecord(body.getString("id"))
                            .map(r -> r.orElseThrow(() -> new NotFoundException("No record with given 'id' to Patch")))
                            .map(r -> r.setModifiedAt(DateTimes.nowUTC()))
                            .map(ITblDashboardConnection::toJson)
                            .map(r -> {
                                body.remove("id");
                                return r.mergeIn(data.body());
                            })
                            .flatMap(r -> entityHandler.updateRecord(r).map(v -> r));
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(
            new ArrayList<>(EdgeDashboardEventModel.EDGE_DASHBOARD_CONNECTION.getEvents()));
    }

}
