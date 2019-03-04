package com.nubeiot.edge.connector.driverapi.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.edge.connector.driverapi.EndpointsMapper;
import com.nubeiot.edge.connector.driverapi.models.DriverEventModels;

import lombok.Getter;
import lombok.NonNull;

public class DynamicEndpointsHandler implements EventHandler {

    private final Vertx vertx;
    @Getter
    private final List<EventAction> availableEvents;
    private EndpointsMapper endpointsMapper;

    public DynamicEndpointsHandler(@NonNull Vertx vertx, @NonNull EventModel eventModel,
                                   @NonNull EndpointsMapper endpointsMapper) {
        this.vertx = vertx;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(eventModel.getEvents()));
        this.endpointsMapper = endpointsMapper;
    }

    @EventContractor(action = EventAction.CREATE, returnType = JsonObject.class)
    public EventMessage addEndpoint(JsonObject data) {

        System.out.println("\n\n\n\n\n\n\n");
        System.out.println("REQUEST RECIEVED TO ADD ENDPOINT");
        System.out.println("\n\n\n\n\n\n\n");

        EventModel eventModel = DriverEventModels.getModel(data.getString("endpoint"));
        EventAction eventAction = EventAction.valueOf(data.getString("action"));
        String driver = data.getString("driver");
        String handlerAddress = data.getString("handlerAddress");

        JsonObject errorMsg = checkMessage(eventModel, eventAction, driver, handlerAddress);
        if (errorMsg != null) {
            return EventMessage.error(EventAction.RETURN, ErrorCode.UNKNOWN_ERROR, "REQUEST ERROR");
        }

        if (endpointsMapper.addEndpointHandler(eventModel, eventAction, driver, handlerAddress)) {
            return EventMessage.success(EventAction.RETURN, getSuccess("Endpoint successfully added"));
        } else {
            return EventMessage.error(EventAction.RETURN, ErrorCode.UNKNOWN_ERROR, "ERROR ADDING ENDPOINT");
        }

        //TODO: try catch with replies
    }


    @EventContractor(action = EventAction.REMOVE, returnType = JsonObject.class)
    public EventMessage removeEndpoint(JsonObject data) {

        EventModel eventModel = DriverEventModels.getModel(data.getString("endpoint"));
        EventAction eventAction = EventAction.valueOf(data.getString("action")); //TODO: check what this returns
        String driver = data.getString("driver");
        String handlerAddress = data.getString("handler");

        JsonObject errorMsg = checkMessage(eventModel, eventAction, driver, handlerAddress);
        if (errorMsg != null) {
            return EventMessage.error(EventAction.RETURN, ErrorCode.UNKNOWN_ERROR, "REQUEST ERROR");
        }

        if (endpointsMapper.removeEndpointHandler(eventModel, eventAction, driver, handlerAddress)) {
            return EventMessage.success(EventAction.RETURN, getSuccess("Endpoint successfully removed"));
        } else {
            return EventMessage.error(EventAction.RETURN, ErrorCode.UNKNOWN_ERROR, "ERROR REMOVING ENDPOINT");
        }
    }

    private JsonObject checkMessage(EventModel eventModel, EventAction eventAction, String driver,
                                    String handlerAddress) {

        if (eventAction == null || driver == null || handlerAddress == null) {
            return getError("resquestError");
        }
        if (eventModel == null) {
            return getError("Invalid endpoint");
        }
        if (!eventModel.getEvents().contains(eventAction)) {
            return getError("Invalid Action");
        }
        return null;
    }

    private JsonObject getSuccess(String message) {
        return new JsonObject("\"success\": \"" + message + "\"");
    }

    private JsonObject getError(String message) {
        return new JsonObject("\"error\": \"" + message + "\"");
    }

}
