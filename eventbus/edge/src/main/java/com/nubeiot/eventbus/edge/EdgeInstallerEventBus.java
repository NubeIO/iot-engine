package com.nubeiot.eventbus.edge;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EdgeInstallerEventBus {

    public static final EventModel SERVICE_DEPLOYMENT = EventModel.builder()
                                                                  .address("nubeiot.edge.app.installer.deployment")
                                                                  .pattern(EventPattern.REQUEST_RESPONSE)
                                                                  .local(true)
                                                                  .addEvents(EventAction.INIT, EventAction.CREATE,
                                                                             EventAction.UPDATE, EventAction.HALT,
                                                                             EventAction.REMOVE)
                                                                  .build();
    public static final EventModel BIOS_INSTALLER = EventModel.builder()
                                                              .address("nubeiot.edge.bios.installer")
                                                              .pattern(EventPattern.REQUEST_RESPONSE)
                                                              .addEvents(EventAction.GET_ONE, EventAction.GET_LIST,
                                                                         EventAction.PATCH)
                                                              .build();
    public static final EventModel BIOS_TRANSACTION = EventModel.builder()
                                                                .address("nubeiot.edge.bios.installer.transaction")
                                                                .pattern(EventPattern.REQUEST_RESPONSE)
                                                                .event(EventAction.GET_ONE)
                                                                .build();
    public static final EventModel BIOS_DEPLOYMENT = EventModel.builder()
                                                               .address("nubeiot.edge.bios.installer.deployment")
                                                               .pattern(EventPattern.REQUEST_RESPONSE)
                                                               .local(true)
                                                               .addEvents(EventAction.INIT, EventAction.CREATE,
                                                                          EventAction.UPDATE, EventAction.PATCH,
                                                                          EventAction.REMOVE)
                                                               .build();
    public static final EventModel BIOS_STATUS = EventModel.builder()
                                                           .address("nubeiot.edge.bios.status")
                                                           .pattern(EventPattern.REQUEST_RESPONSE)
                                                           .event(EventAction.GET_ONE)
                                                           .build();

    public static EventModel getServiceInstaller(boolean local) {
        return EventModel.builder()
                         .address("nubeiot.edge.app.installer")
                         .pattern(EventPattern.REQUEST_RESPONSE)
                         .local(local)
                         .addEvents(EventAction.CREATE, EventAction.UPDATE, EventAction.PATCH, EventAction.REMOVE,
                                    EventAction.GET_ONE, EventAction.GET_LIST)
                         .build();
    }

    public static EventModel getServiceTransaction(boolean local) {
        return EventModel.builder()
                         .address("nubeiot.edge.app.installer.transaction")
                         .pattern(EventPattern.REQUEST_RESPONSE)
                         .local(local)
                         .event(EventAction.GET_ONE)
                         .build();
    }

    public static EventModel getServiceLastTransaction(boolean local) {
        return EventModel.builder()
                         .address("nubeiot.edge.app.installer.last_transaction")
                         .pattern(EventPattern.REQUEST_RESPONSE)
                         .local(local).event(EventAction.GET_LIST)
                         .build();
    }


}
