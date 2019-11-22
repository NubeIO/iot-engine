package com.nubeiot.edge.bios.mock;

import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.service.AppDeploymentFinisher;
import com.nubeiot.edge.installer.service.AppDeploymentTracker;
import com.nubeiot.edge.installer.service.DefaultAppDeployer;

import lombok.NonNull;

public class MockDefaultAppDeployer extends DefaultAppDeployer {

    private EventListener loaderHandler;

    MockDefaultAppDeployer(@NonNull EventModel loaderEvent, @NonNull EventModel trackerEvent,
                           @NonNull EventModel finisherEvent, @NonNull EventListener loaderHandler) {
        super(loaderEvent, trackerEvent, finisherEvent);
        this.loaderHandler = loaderHandler;
    }

    @Override
    public void register(@NonNull InstallerEntityHandler entityHandler) {
        entityHandler.eventClient()
                     .register(getLoaderEvent(), loaderHandler)
                     .register(getTrackerEvent(), new AppDeploymentTracker(entityHandler))
                     .register(getFinisherEvent(), new AppDeploymentFinisher(entityHandler));
    }

}
