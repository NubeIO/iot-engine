package com.nubeiot.edge.module.gateway;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;

class EdgeGatewayEventModel {

    static EventModel infoModel = EventModel.builder()
        .address("edge.gateway")
        .addEvents(EventAction.GET_LIST)
        .build();

}
