package com.nubeiot.edge.connector.driverapi.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.connector.driverapi.EndpointsMapper;
import com.nubeiot.edge.connector.driverapi.models.DriverEventModels;

import lombok.Getter;
import lombok.NonNull;

public class ServerEventHandler implements EventHandler {

    private final Vertx vertx;
    @Getter
    private final List<EventAction> availableEvents;
    private EventController eventController;
    private EndpointsMapper endpointsMapper;

    public ServerEventHandler(@NonNull Vertx vertx, @NonNull EventModel eventModel,
                              @NonNull EventController eventController, @NonNull EndpointsMapper endpointsMapper) {
        this.vertx = vertx;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(eventModel.getEvents()));
        this.eventController = eventController;
        this.endpointsMapper = endpointsMapper;
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = JsonObject.class)
    public Single<JsonObject> getList(RequestData data) {
        if (checkRequest(data.getBody())) {
            String driver = data.getBody().getString("driver").toLowerCase();
            String handlerAddress = endpointsMapper.getDriverHandler(DriverEventModels.POINTS, EventAction.GET_LIST,
                                                                     driver);
            if (handlerAddress == null) {
                return Single.just(getError("Driver handler doesn't exist"));
            }

            //            return SingleHelper.toObserver(Future.future(future -> {
            //                ReplyEventHandler handler = new ReplyEventHandler("DriverAPI", EventAction.GET_LIST,
            //                handlerAddress,
            //                                                                  eventMessage -> {
            //                                                                      future.complete(eventMessage
            //                                                                      .getData());
            //                                                                  }, error -> {
            //                    future.fail(error);
            //                });
            //
            //                eventController.request(handlerAddress, EventPattern.REQUEST_RESPONSE,
            //                                        EventMessage.initial(EventAction.GET_LIST), response -> {
            //                        handler.accept(response);
            //                    });
            //            }).completer());
        } else {
            return Single.just(getError("Driver handler doesn't exist"));
        }
        return null;
    }

    private boolean checkRequest(JsonObject body) {
        String driver = body.getString("driver").toLowerCase();
        if (driver != null && endpointsMapper.driverExists(driver)) {
            return true;
        }
        return false;
    }

    private JsonObject getSuccess(String message) {
        return new JsonObject("{\"success\":\"" + message + "\"}");
    }

    private JsonObject getError(String message) {
        return new JsonObject("{\"error\":\"" + message + "\"}");
    }

}
