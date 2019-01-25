package com.nubeiot.edge.connector.bacnet;

import java.util.Arrays;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

public final class BACnetEventModels {

    public static final EventModel DEVICES = EventModel.builder()
                                                       .address("nubeiot.edge.connector.bacnet.device")
                                                       .pattern(EventPattern.REQUEST_RESPONSE)
                                                       .events(Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE,
                                                                             EventAction.CREATE, EventAction.REMOVE))
                                                       //                                                       .local(true)
                                                       .build();

    public static final EventModel POINTS = EventModel.builder()
                                                      .address("nubeiot.edge.connector.bacnet.device.points")
                                                      .pattern(EventPattern.REQUEST_RESPONSE)
                                                      .events(Arrays.asList(EventAction.GET_LIST))
                                                      //                                                      .local(true)
                                                      .build();

}
