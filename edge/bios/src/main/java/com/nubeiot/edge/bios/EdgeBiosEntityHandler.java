package com.nubeiot.edge.bios;

import org.jooq.Configuration;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.edge.installer.InstallerConfig;
import com.nubeiot.edge.installer.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;
import com.nubeiot.edge.installer.model.tables.pojos.Application;

public final class EdgeBiosEntityHandler extends InstallerEntityHandler {

    protected EdgeBiosEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    protected AppConfig transformAppConfig(RepositoryConfig repoConfig, IApplication application, AppConfig appConfig) {
        if (String.format("%s:%s", "com.nubeiot.edge.module", "installer").equals(application.getAppId())) {
            InstallerConfig installerConfig = new InstallerConfig();
            installerConfig.setRepoConfig(repoConfig);
            return IConfig.merge(new JsonObject().put(installerConfig.key(), installerConfig.toJson()), appConfig,
                                 AppConfig.class);
        }
        return appConfig;
    }

    protected Application decorateApp(Application m) {
        return super.decorateApp(m).setPublishedBy("NubeIO");
    }

}
