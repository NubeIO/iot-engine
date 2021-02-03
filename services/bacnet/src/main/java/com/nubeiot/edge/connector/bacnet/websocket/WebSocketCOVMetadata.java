package com.nubeiot.edge.connector.bacnet.websocket;

import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventModel;
import io.github.zero88.qwe.event.EventPattern;

public class WebSocketCOVMetadata {

    public static final EventModel COV_PUBLISHER = EventModel.builder()
                                                             .address("ws.cov.to.client")
                                                             .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                                             .event(EventAction.RETURN)
                                                             .build();

}
