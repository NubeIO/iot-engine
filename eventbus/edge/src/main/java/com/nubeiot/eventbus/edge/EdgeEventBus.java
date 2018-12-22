package com.nubeiot.eventbus.edge;

import java.util.Arrays;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EdgeEventBus {

    public static final EventModel APP_INSTALLER = EventModel.builder()
                                                             .address("nubeiot.edge.app.installer")
                                                             .pattern(EventPattern.REQUEST_RESPONSE)
                                                             .events(Arrays.asList(EventAction.CREATE,
                                                                                   EventAction.UPDATE, EventAction.HALT,
                                                                                   EventAction.REMOVE,
                                                                                   EventAction.GET_ONE,
                                                                                   EventAction.GET_LIST))
                                                             .build();
    public static final EventModel APP_TRANSACTION = EventModel.builder()
                                                               .address("nubeiot.edge.app.installer.transaction")
                                                               .pattern(EventPattern.REQUEST_RESPONSE)
                                                               .event(EventAction.GET_ONE)
                                                               .build();
    public static final EventModel BIOS_INSTALLER = EventModel.builder()
                                                              .address("nubeiot.edge.bios.installer")
                                                              .pattern(EventPattern.REQUEST_RESPONSE)
                                                              .events(Arrays.asList(EventAction.UPDATE,
                                                                                    EventAction.GET_ONE,
                                                                                    EventAction.GET_LIST,
                                                                                    EventAction.UPDATE))
                                                              .build();
    public static final EventModel BIOS_TRANSACTION = EventModel.builder()
                                                                .address("nubeiot.edge.bios.installer.transaction")
                                                                .pattern(EventPattern.REQUEST_RESPONSE)
                                                                .event(EventAction.GET_ONE)
                                                                .build();
    public static final EventModel BIOS_STATUS = EventModel.builder()
                                                           .address("nubeiot.edge.bios.status")
                                                           .pattern(EventPattern.REQUEST_RESPONSE)
                                                           .event(EventAction.GET_ONE)
                                                           .build();

}
