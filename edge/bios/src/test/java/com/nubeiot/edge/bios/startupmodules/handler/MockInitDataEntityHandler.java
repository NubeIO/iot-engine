package com.nubeiot.edge.bios.startupmodules.handler;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.edge.installer.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.tables.daos.ApplicationDao;
import com.nubeiot.edge.installer.model.tables.daos.DeployTransactionDao;
import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;

abstract class MockInitDataEntityHandler extends InstallerEntityHandler {

    final ApplicationDao applicationDao;
    final DeployTransactionDao tblTransactionDao;

    MockInitDataEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
        this.applicationDao = applicationDao();
        this.tblTransactionDao = transDao();
    }

    //    @Override
    //    public Single<EventMessage> initData() {
    //        final Single<Integer> records = initModules();
    //        final Single<JsonArray> startupModules = this.startAppModules();
    //        return Single.zip(records, startupModules, (r1, r2) -> r1 + r2.size())
    //                     .map(r -> EventMessage.success(EventAction.INIT, new JsonObject().put("records", r)));
    //    }

    @Override
    protected AppConfig transformAppConfig(RepositoryConfig repoConfig, IApplication tblModule, AppConfig appConfig) {
        return appConfig;
    }

    protected abstract Single<Integer> initModules();

}
