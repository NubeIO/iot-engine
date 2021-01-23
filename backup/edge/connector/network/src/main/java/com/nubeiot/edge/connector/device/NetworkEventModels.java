package com.nubeiot.edge.connector.device;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class NetworkEventModels {

    static final EventModel NETWORK_IP = EventModel.builder()
                                                   .address("nubeiot.edge.connector.network.ip")
                                                   .pattern(EventPattern.REQUEST_RESPONSE)
                                                   .local(true).addEvents(EventAction.UPDATE, EventAction.REMOVE)
                                                   .build();

}
