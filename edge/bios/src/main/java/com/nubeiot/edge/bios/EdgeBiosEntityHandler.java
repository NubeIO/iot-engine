package com.nubeiot.edge.bios;

import org.jooq.Configuration;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.core.InstallerConfig;
import com.nubeiot.edge.core.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.RequestedServiceData;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

public final class EdgeBiosEntityHandler extends InstallerEntityHandler {

    protected EdgeBiosEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    protected EventModel deploymentEvent() {
        return InstallerEventModel.BIOS_DEPLOYMENT;
    }

    protected AppConfig transformAppConfig(RepositoryConfig repoConfig, RequestedServiceData serviceData,
                                           ITblModule tblModule, AppConfig appConfig) {
        if (String.format("%s:%s", "com.nubeiot.edge.module", "installer").equals(tblModule.getServiceId())) {
            InstallerConfig installerConfig = new InstallerConfig();
            installerConfig.setRepoConfig(repoConfig);
            appConfig = IConfig.merge(new JsonObject().put(installerConfig.key(), installerConfig.toJson()),
                                      serviceData.getAppConfig(), AppConfig.class);
        }
        return appConfig;
    }

    protected TblModule decorateModule(TblModule m) {
        return super.decorateModule(m).setPublishedBy("NubeIO");
    }

}
