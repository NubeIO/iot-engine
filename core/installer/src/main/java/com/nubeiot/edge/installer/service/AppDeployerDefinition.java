package com.nubeiot.edge.installer.service;

import io.vertx.core.shareddata.Shareable;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.installer.InstallerEntityHandler;

import lombok.NonNull;

/**
 * Application service deployer definition
 *
 * @since 1.0.0
 */
public interface AppDeployerDefinition extends Shareable {

    /**
     * Create app deployer definition.
     *
     * @param loaderEvent   the loader event
     * @param trackerEvent  the tracker event
     * @param finisherEvent the finisher event
     * @return the app deployer
     * @since 1.0.0
     */
    static AppDeployerDefinition create(@NonNull EventModel loaderEvent, @NonNull EventModel trackerEvent,
                                        @NonNull EventModel finisherEvent) {
        return new DefaultAppDeployerDefinition(loaderEvent, trackerEvent, finisherEvent);
    }

    /**
     * Defines deployment loader event
     *
     * @return loader event
     * @since 1.0.0
     */
    @NonNull EventModel getLoaderEvent();

    /**
     * Defines tracker event after finish deploying
     *
     * @return tracker event
     * @since 1.0.0
     */
    @NonNull EventModel getTrackerEvent();

    /**
     * Defines finisher event after finish deploy and update database
     *
     * @return finisher event
     * @since 1.0.0
     */
    @NonNull EventModel getFinisherEvent();

    /**
     * Register event service
     *
     * @param entityHandler Entity handler
     * @since 1.0.0
     */
    void register(@NonNull InstallerEntityHandler entityHandler);

}
