package com.nubeiot.edge.bios;

import org.jooq.Configuration;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.edge.installer.InstallerConfig;
import com.nubeiot.edge.installer.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.tables.interfaces.ITblModule;
import com.nubeiot.edge.installer.model.tables.pojos.TblModule;

public final class EdgeBiosEntityHandler extends InstallerEntityHandler {

    protected EdgeBiosEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    protected AppConfig transformAppConfig(RepositoryConfig repoConfig, ITblModule tblModule, AppConfig appConfig) {
        if (String.format("%s:%s", "com.nubeiot.edge.module", "installer").equals(tblModule.getServiceId())) {
            InstallerConfig installerConfig = new InstallerConfig();
            installerConfig.setRepoConfig(repoConfig);
            return IConfig.merge(new JsonObject().put(installerConfig.key(), installerConfig.toJson()), appConfig,
                                 AppConfig.class);
        }
        return appConfig;
    }

    protected TblModule decorateModule(TblModule m) {
        return super.decorateModule(m).setPublishedBy("NubeIO");
    }

}
