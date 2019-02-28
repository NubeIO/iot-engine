package com.nubeiot.edge.connector.driverapi;

import java.util.Arrays;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

public final class DriverEventModels {

    public static final EventModel POINTS = EventModel.builder()
                                                      .address("nubeiot.edge.connector.driverapi.points")
                                                      .pattern(EventPattern.REQUEST_RESPONSE)
                                                      .events(Arrays.asList(EventAction.GET_LIST))
                                                      .build();

}
