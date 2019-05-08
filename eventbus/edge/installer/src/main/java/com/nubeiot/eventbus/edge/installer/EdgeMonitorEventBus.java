package com.nubeiot.eventbus.edge;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EdgeMonitorEventBus {

    public static EventModel getMonitorStatus(boolean local) {
        return EventModel.builder()
                         .address("nubeiot.edge.module.monitor.status")
                         .pattern(EventPattern.REQUEST_RESPONSE)
                         .local(local)
                         .addEvents(EventAction.GET_LIST)
                         .build();
    }

    public static EventModel getMonitorNetworkStatus(boolean local) {
        return EventModel.builder()
                         .address("nubeiot.edge.module.monitor.network.status")
                         .pattern(EventPattern.REQUEST_RESPONSE)
                         .local(local)
                         .addEvents(EventAction.GET_LIST)
                         .build();
    }

}
