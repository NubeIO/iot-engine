package com.nubeiot.eventbus.edge;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EdgeEventBus {

    public static final EventModel APP_INSTALLER = new EventModel("nubeiot.edge.app.installer").add(EventType.CREATE,
                                                                                                    EventType.UPDATE,
                                                                                                    EventType.HALT,
                                                                                                    EventType.REMOVE,
                                                                                                    EventType.GET_ONE,
                                                                                                    EventType.GET_LIST);
    public static final EventModel APP_TRANSACTION = new EventModel("nubeiot.edge.app.installer.transaction").add(
            EventType.GET_ONE);
    public static final EventModel BIOS_INSTALLER = new EventModel("nubeiot.edge.bios.installer").add(EventType.UPDATE,
                                                                                                      EventType.GET_ONE,
                                                                                                      EventType.GET_LIST,
                                                                                                      EventType.UPDATE);
    public static final EventModel BIOS_TRANSACTION = new EventModel("nubeiot.edge.bios.installer.transaction").add(
            EventType.GET_ONE);
    public static final EventModel BIOS_STATUS = new EventModel("nubeiot.edge.bios.status").add(EventType.GET_ONE);

}
