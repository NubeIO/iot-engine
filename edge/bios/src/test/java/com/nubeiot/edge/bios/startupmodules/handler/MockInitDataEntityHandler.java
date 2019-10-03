package com.nubeiot.edge.bios.startupmodules.handler;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.tables.daos.TblTransactionDao;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

public abstract class MockInitDataEntityHandler extends InstallerEntityHandler {

    protected final TblModuleDao tblModuleDao;
    protected final TblTransactionDao tblTransactionDao;

    protected MockInitDataEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
        this.tblModuleDao = dao(TblModuleDao.class);
        this.tblTransactionDao = dao(TblTransactionDao.class);
    }

    @Override
    public Single<EventMessage> initData() {
        final Single<Integer> records = initModules();
        final Single<JsonObject> startupModules = this.startupModules();
        return Single.zip(records, startupModules, (r1, r2) -> r1 + r2.toString())
                     .map(r -> EventMessage.success(EventAction.INIT, new JsonObject().put("records", r)));
    }

    protected abstract Single<Integer> initModules();

    @Override
    protected EventModel deploymentEvent() {
        return InstallerEventModel.BIOS_DEPLOYMENT;
    }

}
