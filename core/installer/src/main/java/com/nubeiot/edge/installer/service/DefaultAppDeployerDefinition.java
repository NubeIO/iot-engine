package com.nubeiot.edge.installer.service;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.installer.InstallerEntityHandler;

import lombok.Getter;
import lombok.NonNull;

@Getter
final class DefaultAppDeployerDefinition implements AppDeployerDefinition {

    @NonNull
    private final EventModel executerEvent;
    @NonNull
    private final EventModel supervisorEvent;
    @NonNull
    private final EventModel reporterEvent;

    public DefaultAppDeployerDefinition(@NonNull String executerAddress, @NonNull String supervisorAddress,
                                        @NonNull String reporterAddress) {
        this.executerEvent = AppDeployerDefinition.createExecuterEvent(executerAddress);
        this.supervisorEvent = AppDeployerDefinition.createSupervisorEvent(supervisorAddress);
        this.reporterEvent = AppDeployerDefinition.createReporterEvent(reporterAddress);
    }

    @Override
    public AppDeployerDefinition register(@NonNull InstallerEntityHandler entityHandler) {
        entityHandler.eventClient()
                     .register(executerEvent, new AppDeploymentExecuter(entityHandler))
                     .register(supervisorEvent, new AppDeploymentSupervisor(entityHandler))
                     .register(reporterEvent, new AppDeploymentReporter(entityHandler));
        return this;
    }

}
