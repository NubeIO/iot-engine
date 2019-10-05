package com.nubeiot.eventbus.edge.installer;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InstallerEventModel {

    public static final EventModel SERVICE_DEPLOYMENT = EventModel.builder()
                                                                  .address("nubeiot.edge.app.installer.deployment")
                                                                  .pattern(EventPattern.POINT_2_POINT)
                                                                  .local(true)
                                                                  .addEvents(EventAction.INIT, EventAction.CREATE,
                                                                             EventAction.UPDATE, EventAction.HALT,
                                                                             EventAction.REMOVE)
                                                                  .build();
    public static final EventModel SERVICE_POST_DEPLOYMENT = EventModel.builder()
                                                                       .address(
                                                                           "nubeiot.edge.app.installer.deployment.post")
                                                                       .pattern(EventPattern.POINT_2_POINT)
                                                                       .local(true)
                                                                       .event(EventAction.MONITOR)
                                                                       .build();
    public static final EventModel BIOS_DEPLOYMENT = EventModel.builder()
                                                               .address("nubeiot.edge.bios.installer.deployment")
                                                               .pattern(EventPattern.POINT_2_POINT)
                                                               .local(true)
                                                               .addEvents(EventAction.INIT, EventAction.CREATE,
                                                                          EventAction.UPDATE, EventAction.PATCH,
                                                                          EventAction.REMOVE)
                                                               .build();
    public static final EventModel BIOS_POST_DEPLOYMENT = EventModel.builder()
                                                                    .address(
                                                                        "nubeiot.edge.bios.installer.deployment.post")
                                                                    .pattern(EventPattern.POINT_2_POINT)
                                                                    .local(true)
                                                                    .event(EventAction.MONITOR)
                                                                    .build();

}
