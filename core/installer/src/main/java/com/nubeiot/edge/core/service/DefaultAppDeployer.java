package com.nubeiot.edge.core.service;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.core.InstallerEntityHandler;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
final class DefaultAppDeployer implements AppDeployer {

    @NonNull
    private final EventModel loaderEvent;
    @NonNull
    private final EventModel trackerEvent;
    @NonNull
    private final EventModel finisherEvent;

    @Override
    public void register(@NonNull InstallerEntityHandler entityHandler) {
        entityHandler.eventClient()
                     .register(getLoaderEvent(), new AppDeploymentService(entityHandler))
                     .register(getTrackerEvent(), new AppDeploymentTracker(entityHandler))
                     .register(getFinisherEvent(), new AppDeploymentFinisher(entityHandler));
    }

}
