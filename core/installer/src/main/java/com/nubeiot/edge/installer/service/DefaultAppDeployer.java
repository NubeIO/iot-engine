package com.nubeiot.edge.installer.service;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.installer.InstallerEntityHandler;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DefaultAppDeployer implements AppDeployer {

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
