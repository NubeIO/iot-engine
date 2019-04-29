package com.nubeiot.edge.connector.bacnet;

import java.util.Arrays;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

public final class BACnetEventModels {

    public static final EventModel NUBE_SERVICE_SUB = EventModel.builder()
                                                                //TODO: verify address
                                                                .address("nubeiot.edge.connector.edgeapi")
                                                                .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                                                .local(true)
                                                                .events(Arrays.asList(EventAction.CREATE,
                                                                                      EventAction.REMOVE,
                                                                                      EventAction.UPDATE,
                                                                                      EventAction.PATCH))
                                                                .build();

    public static final EventModel NETWORKS_ALL = EventModel.builder()
                                                            .address("nubeiot.edge.connector.bacnet")
                                                            .pattern(EventPattern.REQUEST_RESPONSE)
                                                            .local(true)
                                                            .events(Arrays.asList(EventAction.GET_LIST,
                                                                                  EventAction.GET_ONE))
                                                            .build();

    public static final EventModel DEVICES = EventModel.builder()
                                                       .address("nubeiot.edge.connector.bacnet.device")
                                                       .pattern(EventPattern.REQUEST_RESPONSE)
                                                       .local(true)
                                                       .events(Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE))
                                                       .build();

    public static final EventModel POINTS = EventModel.builder()
                                                      .address("nubeiot.edge.connector.bacnet.device.points")
                                                      .pattern(EventPattern.REQUEST_RESPONSE)
                                                      .local(true)
                                                      .events(Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE,
                                                                            EventAction.PATCH, EventAction.CREATE,
                                                                            EventAction.REMOVE))
                                                      .build();

    //TODO: what's edge-api global eventbus publish address??
    public static final EventModel GLOBAL_PUBLISHES = EventModel.builder()
                                                                .address("nubeiot.edge.connector.bacnet")
                                                                .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                                                .local(true)
                                                                .events(Arrays.asList(EventAction.GET_LIST,
                                                                                      EventAction.CREATE,
                                                                                      EventAction.REMOVE))
                                                                .build();

}
