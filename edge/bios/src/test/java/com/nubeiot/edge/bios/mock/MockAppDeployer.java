package com.nubeiot.edge.bios.mock;

import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.installer.service.AppDeployer;

import lombok.NonNull;

public interface MockAppDeployer extends AppDeployer {

    static AppDeployer create(@NonNull EventModel loaderEvent, @NonNull EventModel trackerEvent,
                              @NonNull EventModel finisherEvent, @NonNull EventListener loaderHandler) {
        return new MockDefaultAppDeployer(loaderEvent, trackerEvent, finisherEvent, loaderHandler);
    }

}
