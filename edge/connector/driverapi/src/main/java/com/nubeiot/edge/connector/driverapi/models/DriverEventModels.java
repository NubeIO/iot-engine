package com.nubeiot.edge.connector.driverapi.models;

import java.util.Arrays;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

public class DriverEventModels {

    public static final EventModel POINTS = EventModel.builder()
                                                      .address("nubeiot.edge.connector.driverapi.points")
                                                      .pattern(EventPattern.REQUEST_RESPONSE)
                                                      .events(Arrays.asList(EventAction.GET_LIST))
                                                      .local(true)
                                                      .build();

    public static final EventModel ENDPOINTS = EventModel.builder()
                                                         .address("nubeiot.edge.connector.driverapi.endpoints")
                                                         .pattern(EventPattern.REQUEST_RESPONSE)
                                                         .events(Arrays.asList(EventAction.CREATE, EventAction.REMOVE))
                                                         .local(true)
                                                         .build();

    private static final EventModel[] httpEventModels = {POINTS};

    public static final EventModel[] getAllHttpModels() {
        return httpEventModels;
    }

    public static final EventModel getModel(String name) {
        switch (name.toLowerCase()) {
            case "points":
                return POINTS;
            default:
                return null;
        }
    }

}
