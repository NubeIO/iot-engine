package com.nubeiot.edge.installer.service;

import com.nubeiot.edge.installer.InstallerEntityHandler;

//TODO extends this for success/failed/timeout case and assert PostDeploymentResult
public class MockFinisherService extends AppDeploymentFinisher {

    MockFinisherService(InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
