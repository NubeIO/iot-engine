package com.nubeiot.edge.bios.startupmodules.handler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.tables.daos.TblTransactionDao;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.edge.core.model.tables.pojos.TblTransaction;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

public abstract class MockInitDataEntityHandler extends EdgeEntityHandler {

    protected final TblModuleDao tblModuleDao;
    protected final TblTransactionDao tblTransactionDao;

    protected MockInitDataEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
        this.tblModuleDao = new TblModuleDao(jooqConfig, getVertx());
        this.tblTransactionDao = new TblTransactionDao(jooqConfig, getVertx());
    }

    @Override
    public Single<EventMessage> initData() {
        final Single<Integer> records = initModules();
        final Single<JsonObject> startupModules = this.startupModules();
        return Single.zip(records, startupModules, (r1, r2) -> r1.intValue() + r2.toString())
                     .map(r -> EventMessage.success(EventAction.INIT, new JsonObject().put("records", r)));
    }

    protected abstract Single<Integer> initModules();

    @Override
    protected EventModel deploymentEvent() {
        return EdgeInstallerEventBus.BIOS_DEPLOYMENT;
    }

}
