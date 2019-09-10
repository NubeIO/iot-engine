package com.nubeiot.eventbus.edge.gateway;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

public class GatewayEventBus {

    public static final EventModel DRIVER_REGISTRATION = EventModel.builder()
                                                                   .address("nubeiot.eventbus.edge.gateway.driver" +
                                                                            ".registration")
                                                                   .pattern(EventPattern.REQUEST_RESPONSE)
                                                                   .local(true)
                                                                   .addEvents(EventAction.CREATE, EventAction.REMOVE)
                                                                   .build();

}
