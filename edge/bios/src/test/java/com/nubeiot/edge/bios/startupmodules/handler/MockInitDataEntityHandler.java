package com.nubeiot.edge.bios.startupmodules.handler;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.edge.installer.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.installer.model.tables.daos.TblTransactionDao;
import com.nubeiot.edge.installer.model.tables.interfaces.ITblModule;

abstract class MockInitDataEntityHandler extends InstallerEntityHandler {

    final TblModuleDao tblModuleDao;
    final TblTransactionDao tblTransactionDao;

    MockInitDataEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
        this.tblModuleDao = dao(TblModuleDao.class);
        this.tblTransactionDao = dao(TblTransactionDao.class);
    }

    //    @Override
    //    public Single<EventMessage> initData() {
    //        final Single<Integer> records = initModules();
    //        final Single<JsonArray> startupModules = this.startAppModules();
    //        return Single.zip(records, startupModules, (r1, r2) -> r1 + r2.size())
    //                     .map(r -> EventMessage.success(EventAction.INIT, new JsonObject().put("records", r)));
    //    }

    @Override
    protected AppConfig transformAppConfig(RepositoryConfig repoConfig, ITblModule tblModule, AppConfig appConfig) {
        return appConfig;
    }

    protected abstract Single<Integer> initModules();

}
