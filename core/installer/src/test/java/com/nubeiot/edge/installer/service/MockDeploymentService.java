package com.nubeiot.edge.installer.service;

import java.util.UUID;

import io.vertx.core.Future;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.dto.PreDeploymentResult;

import lombok.NonNull;

//TODO extends this for success/failed/timeout case and assert PreDeploymentResult
public class MockDeploymentService extends AppDeploymentService {

    @NonNull
    private final UUID mockDeployId;

    MockDeploymentService(@NonNull InstallerEntityHandler entityHandler, @NonNull UUID mockDeployId) {
        super(entityHandler);
        this.mockDeployId = mockDeployId;
    }

    @Override
    void doDeploy(PreDeploymentResult preResult, Future<String> future) {
        future.complete(mockDeployId.toString());
    }

    @Override
    void doUnDeploy(PreDeploymentResult preResult, boolean silent, Future<String> future) {
        future.complete(mockDeployId.toString());
    }

}
