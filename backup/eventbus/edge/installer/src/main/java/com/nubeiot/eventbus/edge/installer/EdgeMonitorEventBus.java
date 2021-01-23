package com.nubeiot.eventbus.edge.installer;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EdgeMonitorEventBus {

    public static final EventModel MONITOR_STATUS = EventModel.builder()
                                                              .address("com.nubeiot.edge.module.monitor.service" +
                                                                       ".MonitorStatusService")
                                                              .pattern(EventPattern.REQUEST_RESPONSE)
                                                              .local(true)
                                                              .addEvents(EventAction.GET_LIST)
                                                              .build();

    public static final EventModel MONITOR_NETWORK = EventModel.builder()
                                                               .address("com.nubeiot.edge.module.monitor.service" +
                                                                        ".MonitorNetworkStatusService")
                                                               .pattern(EventPattern.REQUEST_RESPONSE)
                                                               .local(true)
                                                               .addEvents(EventAction.GET_LIST)
                                                               .build();

}
