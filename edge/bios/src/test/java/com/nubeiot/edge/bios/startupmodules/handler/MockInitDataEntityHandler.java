package com.nubeiot.edge.bios.startupmodules.handler;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.installer.model.tables.daos.TblTransactionDao;

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
        return initModules().map(r -> EventMessage.success(EventAction.INIT, new JsonObject().put("records", r)))
                            .flatMap(r -> migrate());
    }

    protected abstract Single<Integer> initModules();

}
