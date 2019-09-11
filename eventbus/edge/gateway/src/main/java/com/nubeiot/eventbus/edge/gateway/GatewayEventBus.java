package com.nubeiot.eventbus.edge.gateway;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

public class GatewayEventBus {

    public static final EventModel ROUTER_REGISTRATION = EventModel.builder()
                                                                   .pattern(EventPattern.REQUEST_RESPONSE)
                                                                   .addEvents(EventAction.CREATE, EventAction.REMOVE)
                                                                   .address(GatewayEventBus.class.getName() +
                                                                            ".registration")
                                                                   .local(true)
                                                                   .build();

    public static final EventModel ROUTER_ANNOUNCEMENT = EventModel.builder()
                                                                   .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                                                   .event(EventAction.MONITOR)
                                                                   .address(GatewayEventBus.class.getName() +
                                                                            ".announcement")
                                                                   .local(true)
                                                                   .build();

}
