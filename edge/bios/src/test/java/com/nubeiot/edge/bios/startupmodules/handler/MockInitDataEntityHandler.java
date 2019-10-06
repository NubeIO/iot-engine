package com.nubeiot.edge.bios.startupmodules.handler;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.core.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.RequestedServiceData;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.tables.daos.TblTransactionDao;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

abstract class MockInitDataEntityHandler extends InstallerEntityHandler {

    final TblModuleDao tblModuleDao;
    final TblTransactionDao tblTransactionDao;

    MockInitDataEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
        this.tblModuleDao = dao(TblModuleDao.class);
        this.tblTransactionDao = dao(TblTransactionDao.class);
    }

    @Override
    public Single<EventMessage> initData() {
        final Single<Integer> records = initModules();
        final Single<JsonObject> startupModules = this.startAppModules();
        return Single.zip(records, startupModules, (r1, r2) -> r1 + r2.toString())
                     .map(r -> EventMessage.success(EventAction.INIT, new JsonObject().put("records", r)));
    }

    @Override
    protected EventModel deploymentEvent() {
        return InstallerEventModel.BIOS_DEPLOYMENT;
    }

    @Override
    protected AppConfig transformAppConfig(RepositoryConfig repoConfig, RequestedServiceData serviceData,
                                           ITblModule tblModule, AppConfig appConfig) {
        return appConfig;
    }

    protected abstract Single<Integer> initModules();

}
