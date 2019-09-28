package com.nubeiot.edge.connector.bacnet;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

public final class BACnetEventModels {

    public static final EventModel NUBE_SERVICE = EventModel.builder()
                                                            .address("nubeiot.edge.connector.bacnet.local")
                                                            .pattern(EventPattern.REQUEST_RESPONSE)
                                                            .local(true)
                                                            .addEvents(EventAction.CREATE, EventAction.REMOVE,
                                                                       EventAction.UPDATE, EventAction.PATCH)
                                                            .build();

    public static final EventModel NETWORKS_ALL = EventModel.builder()
                                                            .address("nubeiot.edge.connector.bacnet.all")
                                                            .pattern(EventPattern.REQUEST_RESPONSE)
                                                            .local(true)
                                                            .addEvents(EventAction.GET_LIST, EventAction.UPDATE)
                                                            .build();

    public static final EventModel DEVICES = EventModel.builder()
                                                       .address("nubeiot.edge.connector.bacnet.device")
                                                       .pattern(EventPattern.REQUEST_RESPONSE)
                                                       .local(true).addEvents(EventAction.GET_LIST, EventAction.GET_ONE)
                                                       .build();

    public static final EventModel POINTS_INFO = EventModel.builder()
                                                           .address("nubeiot.edge.connector.bacnet.device.points-info")
                                                           .pattern(EventPattern.REQUEST_RESPONSE)
                                                           .local(true)
                                                           .addEvents(EventAction.GET_LIST, EventAction.GET_ONE)
                                                           .build();

    public static final EventModel POINTS = EventModel.builder()
                                                      .address("nubeiot.edge.connector.bacnet.device.points")
                                                      .pattern(EventPattern.REQUEST_RESPONSE)
                                                      .local(true)
                                                      .addEvents(EventAction.GET_LIST, EventAction.GET_ONE,
                                                                 EventAction.PATCH)
                                                      .build();

}
