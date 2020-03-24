package com.nubeiot.edge.installer.service;

import com.nubeiot.edge.installer.InstallerEntityHandler;

//TODO extends this for success/failed/timeout case and assert PostDeploymentResult
public class MockFinisherService extends AppDeploymentFinisher {

    protected MockFinisherService(InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
