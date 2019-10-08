package com.nubeiot.edge.bios.startupmodules.handler;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.installer.loader.ModuleType;
import com.nubeiot.edge.installer.model.tables.pojos.TblModule;

public class EnabledModuleInitData extends MockInitDataEntityHandler {

    protected EnabledModuleInitData(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    protected Single<Integer> initModules() {
        return tblModuleDao.insert(new TblModule().setServiceId("enabled-service")
                                                  .setServiceName("service0")
                                                  .setServiceType(ModuleType.JAVA)
                                                  .setVersion("1.0.0")
                                                  .setState(State.ENABLED)
                                                  .setCreatedAt(DateTimes.now())
                                                  .setModifiedAt(DateTimes.now())
                                                  .setSystemConfig(new JsonObject())
                                                  .setAppConfig(new JsonObject()));
    }

}
