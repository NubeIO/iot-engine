package com.nubeiot.edge.installer.service;

import io.vertx.core.shareddata.Shareable;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.edge.installer.InstallerEntityHandler;

import lombok.NonNull;

/**
 * Application service deployer definition
 *
 * @since 1.0.0
 */
public interface AppDeployerDefinition extends Shareable {

    static AppDeployerDefinition create(@NonNull String app) {
        return create(AppDeployerDefinition.createExecuterAddr(app), AppDeployerDefinition.createSupervisorAddr(app),
                      AppDeployerDefinition.createReporterAddr(app));
    }

    /**
     * Create app deployer definition.
     *
     * @param executerAddress   the executer event
     * @param supervisorAddress the supervisor event
     * @param reporterAddress   the reporter event
     * @return the app deployer
     * @since 1.0.0
     */
    static AppDeployerDefinition create(@NonNull String executerAddress, @NonNull String supervisorAddress,
                                        @NonNull String reporterAddress) {
        return new DefaultAppDeployerDefinition(executerAddress, supervisorAddress, reporterAddress);
    }

    static String createExecuterAddr(String app) {
        return AppDeployerDefinition.class.getPackage().getName() + "." + app + ".deployment.executer";
    }

    static String createSupervisorAddr(String app) {
        return AppDeployerDefinition.class.getPackage().getName() + "." + app + ".deployment.supervisor";
    }

    static String createReporterAddr(String app) {
        return AppDeployerDefinition.class.getPackage().getName() + "." + app + ".deployment.reporter";
    }

    static EventModel createExecuterEvent(@NonNull String address) {
        return EventModel.builder()
                         .address(address)
                         .pattern(EventPattern.POINT_2_POINT)
                         .local(true)
                         .addEvents(EventAction.INIT, EventAction.CREATE, EventAction.UPDATE, EventAction.PATCH,
                                    EventAction.REMOVE)
                         .build();
    }

    static EventModel createReporterEvent(@NonNull String address) {
        return EventModel.builder()
                         .address(address)
                         .pattern(EventPattern.POINT_2_POINT)
                         .local(true)
                         .event(EventAction.NOTIFY)
                         .build();
    }

    static EventModel createSupervisorEvent(@NonNull String address) {
        return EventModel.builder()
                         .address(address)
                         .pattern(EventPattern.POINT_2_POINT)
                         .local(true)
                         .event(EventAction.MONITOR)
                         .build();
    }

    /**
     * Defines executer event
     *
     * @return executer event
     * @since 1.0.0
     */
    @NonNull EventModel getExecuterEvent();

    /**
     * Defines supervisor event after deployed application physically and in charge of updating database
     *
     * @return supervisor event
     * @since 1.0.0
     */
    @NonNull EventModel getSupervisorEvent();

    /**
     * Defines reporter event after deployed and updated database completely
     *
     * @return reporter event
     * @since 1.0.0
     */
    @NonNull EventModel getReporterEvent();

    /**
     * Register event service
     *
     * @param entityHandler Entity handler
     * @return a reference to this, so the API can be used fluently
     * @since 1.0.0
     */
    @NonNull AppDeployerDefinition register(@NonNull InstallerEntityHandler entityHandler);

}
