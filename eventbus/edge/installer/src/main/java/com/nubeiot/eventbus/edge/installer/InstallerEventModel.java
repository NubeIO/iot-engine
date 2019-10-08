package com.nubeiot.eventbus.edge.installer;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InstallerEventModel {

    public static final EventModel SERVICE_DEPLOYMENT = EventModel.builder().address(deployAddr("app"))
                                                                  .pattern(EventPattern.POINT_2_POINT)
                                                                  .local(true)
                                                                  .addEvents(EventAction.INIT, EventAction.CREATE,
                                                                             EventAction.UPDATE, EventAction.HALT,
                                                                             EventAction.REMOVE)
                                                                  .build();
    public static final EventModel SERVICE_DEPLOYMENT_TRACKER = EventModel.builder()
                                                                          .address(trackerAddr("app"))
                                                                          .pattern(EventPattern.POINT_2_POINT)
                                                                          .local(true)
                                                                          .event(EventAction.MONITOR)
                                                                          .build();
    public static final EventModel SERVICE_DEPLOYMENT_FINISHER = EventModel.builder()
                                                                           .address(finisherAddr("app"))
                                                                           .pattern(EventPattern.POINT_2_POINT)
                                                                           .local(true)
                                                                           .event(EventAction.NOTIFY)
                                                                           .build();

    public static final EventModel BIOS_DEPLOYMENT = EventModel.builder().address(deployAddr("bios"))
                                                               .pattern(EventPattern.POINT_2_POINT)
                                                               .local(true)
                                                               .addEvents(EventAction.INIT, EventAction.CREATE,
                                                                          EventAction.UPDATE, EventAction.PATCH,
                                                                          EventAction.REMOVE)
                                                               .build();
    public static final EventModel BIOS_DEPLOYMENT_TRACKER = EventModel.builder()
                                                                       .address(trackerAddr("bios"))
                                                                       .pattern(EventPattern.POINT_2_POINT)
                                                                       .local(true)
                                                                       .event(EventAction.MONITOR)
                                                                       .build();
    public static final EventModel BIOS_DEPLOYMENT_FINISHER = EventModel.builder()
                                                                        .address(finisherAddr("bios"))
                                                                        .pattern(EventPattern.POINT_2_POINT)
                                                                        .local(true)
                                                                        .event(EventAction.NOTIFY)
                                                                        .build();

    private static String deployAddr(String app) {
        return InstallerEventModel.class.getPackage().getName() + "." + app + ".deployment";
    }

    private static String trackerAddr(String app) {
        return InstallerEventModel.class.getPackage().getName() + "." + app + ".deployment.tracker";
    }

    private static String finisherAddr(String app) {
        return InstallerEventModel.class.getPackage().getName() + "." + app + ".deployment.finisher";
    }

}
