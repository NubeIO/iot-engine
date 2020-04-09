package com.nubeiot.edge.installer.service;

import com.nubeiot.edge.installer.InstallerEntityHandler;

//TODO extends this for success/failed/timeout case and assert PostDeploymentResult
public class MockReporterService extends AppDeploymentReporter {

    protected MockReporterService(InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
