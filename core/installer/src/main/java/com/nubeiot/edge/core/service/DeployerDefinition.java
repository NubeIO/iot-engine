package com.nubeiot.edge.core.service;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.core.InstallerEntityHandler;

import lombok.NonNull;

public interface DeployerDefinition {

    static DeployerDefinition createDefault(@NonNull EventModel loaderEvent, @NonNull EventModel postLoader,
                                            @NonNull InstallerEntityHandler entityHandler) {
        return new DefaultDeployerDefinition(loaderEvent, postLoader,
                                             new DeployerService(entityHandler.vertx(), entityHandler::sharedData,
                                                                 postLoader), new DeployerPostService(entityHandler));
    }

    static DeployerDefinition create(@NonNull EventModel loaderEvent, @NonNull EventModel postLoaderEvent,
                                     @NonNull EventListener loaderHandler, @NonNull EventListener postLoaderHandler) {
        return new DefaultDeployerDefinition(loaderEvent, postLoaderEvent, loaderHandler, postLoaderHandler);
    }

    default void register(@NonNull EventController eventClient) {
        eventClient.register(getEvent(), getHandler()).register(getPostEvent(), getPostHandler());
    }

    EventModel getEvent();

    EventModel getPostEvent();

    EventListener getHandler();

    EventListener getPostHandler();

}
