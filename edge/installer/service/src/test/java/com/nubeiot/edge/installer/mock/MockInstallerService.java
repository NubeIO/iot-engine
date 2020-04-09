package com.nubeiot.edge.installer.mock;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.service.ApplicationService;
import com.nubeiot.edge.installer.service.InstallerService;
import com.nubeiot.edge.installer.service.TransactionService;

import lombok.NonNull;

public interface MockInstallerService extends InstallerService {

    default String api() {
        return "mock.installer." + this.getClass().getSimpleName();
    }

    default String appPath() {
        return "/app";
    }

    class MockApplicationService extends ApplicationService implements MockInstallerService {

        public MockApplicationService(@NonNull InstallerEntityHandler entityHandler) {
            super(entityHandler);
        }

    }


    class MockTransactionService extends TransactionService implements MockInstallerService {

        public MockTransactionService(@NonNull InstallerEntityHandler entityHandler) {
            super(entityHandler);
        }

    }

}
