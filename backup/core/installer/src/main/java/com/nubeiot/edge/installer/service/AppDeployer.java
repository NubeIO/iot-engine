package com.nubeiot.edge.installer.service;

import io.vertx.core.shareddata.Shareable;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.installer.InstallerEntityHandler;

import lombok.NonNull;

/**
 * Application service deployer definition
 */
public interface AppDeployer extends Shareable {

    static AppDeployer create(@NonNull EventModel loaderEvent, @NonNull EventModel trackerEvent,
                              @NonNull EventModel finisherEvent) {
        return new DefaultAppDeployer(loaderEvent, trackerEvent, finisherEvent);
    }

    /**
     * Defines deployment loader event
     *
     * @return loader event
     */
    @NonNull EventModel getLoaderEvent();

    /**
     * Defines tracker event after finish deploying
     *
     * @return tracker event
     */
    @NonNull EventModel getTrackerEvent();

    /**
     * Defines finisher event after finish deploy and update database
     *
     * @return finisher event
     */
    @NonNull EventModel getFinisherEvent();

    /**
     * Register event service
     *
     * @param entityHandler Entity handler
     */
    void register(@NonNull InstallerEntityHandler entityHandler);

}
