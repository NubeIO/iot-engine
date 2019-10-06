package com.nubeiot.edge.core.service;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.core.InstallerEntityHandler;

import lombok.NonNull;

public interface AppDeployer {

    static AppDeployer createDefault(@NonNull EventModel loaderEvent, @NonNull EventModel postLoader,
                                     @NonNull InstallerEntityHandler entityHandler) {
        return new DefaultAppDeployer(loaderEvent, postLoader,
                                      new AppDeploymentService(entityHandler.vertx(), entityHandler::sharedData,
                                                               postLoader), new AppDeploymentTracker(entityHandler));
    }

    static AppDeployer create(@NonNull EventModel loaderEvent, @NonNull EventModel postLoaderEvent,
                              @NonNull EventListener loaderHandler, @NonNull EventListener postLoaderHandler) {
        return new DefaultAppDeployer(loaderEvent, postLoaderEvent, loaderHandler, postLoaderHandler);
    }

    default void register(@NonNull EventController eventClient) {
        eventClient.register(getEvent(), getHandler()).register(getTrackerEvent(), getTracker());
    }

    EventModel getEvent();

    EventModel getTrackerEvent();

    EventListener getHandler();

    EventListener getTracker();

}
