package com.nubeiot.edge.connector.bacnet;

import java.util.Arrays;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

public final class BACnetEventModels {

    //TODO: need more models for saved devices / all devices / how will this work
    public static final EventModel DEVICES = EventModel.builder()
                                                       .address("nubeiot.edge.connector.bacnet.device")
                                                       .pattern(EventPattern.REQUEST_RESPONSE)
                                                       .local(true)
                                                       .events(Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE,
                                                                             EventAction.CREATE, EventAction.REMOVE))
                                                       .build();

    public static final EventModel POINTS = EventModel.builder()
                                                      .address("nubeiot.edge.connector.bacnet.device.points")
                                                      .pattern(EventPattern.REQUEST_RESPONSE).local(true)
                                                      .events(Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE,
                                                                            EventAction.CREATE, EventAction.REMOVE,
                                                                            EventAction.PATCH))
                                                      .build();

}
