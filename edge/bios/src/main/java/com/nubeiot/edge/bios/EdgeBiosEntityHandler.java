package com.nubeiot.edge.bios;

import java.nio.file.Path;
import java.util.List;

import org.jooq.Configuration;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.InstallerConfig;
import com.nubeiot.edge.core.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.core.RequestedServiceData;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

public final class EdgeBiosEntityHandler extends EdgeEntityHandler {

    protected EdgeBiosEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    public Single<EventMessage> initData() {
        return bootstrap(EventAction.INIT);
    }

    @Override
    public Single<EventMessage> migrate() {
        return bootstrap(EventAction.MIGRATE);
    }

    @Override
    protected EventModel deploymentEvent() {
        return InstallerEventModel.BIOS_DEPLOYMENT;
    }

    private Single<EventMessage> bootstrap(EventAction action) {
        InstallerConfig installerCfg = IConfig.from(sharedData(EdgeBiosVerticle.SHARED_INSTALLER_CFG),
                                                    InstallerConfig.class);
        logger.debug("Shared app configuration: {}", installerCfg);
        setupServiceRepository(installerCfg.getRepoConfig());
        return this.isFreshInstall()
                   .flatMap(f -> startup(dataDir(), installerCfg, f).map(r -> EventMessage.success(action, r)));
    }

    private Single<JsonObject> startup(Path dataDir, InstallerConfig installerCfg, boolean isFresh) {
        if (!isFresh) {
            return this.startupModules();
        }
        if (installerCfg.isAutoInstall()) {
            return initInstaller(dataDir, installerCfg.getRepoConfig(), installerCfg.getBuiltinApps());
        }
        return Single.just(new JsonObject());
    }

    private Single<JsonObject> initInstaller(Path dataDir, RepositoryConfig repoConfig,
                                             List<RequestedServiceData> builtinApps) {
        if (builtinApps.isEmpty()) {
            return Single.just(new JsonObject().put("success", true));
        }
        return Observable.fromIterable(builtinApps)
                         .flatMapSingle(serviceData -> processDeploymentTransaction(
                             createTblModule(dataDir, repoConfig, serviceData), EventAction.INIT))
                         .toList()
                         .map(results -> new JsonObject().put("results", results));
    }

    private ITblModule createTblModule(Path dataDir, RepositoryConfig repoConfig, RequestedServiceData serviceData) {
        ModuleTypeRule rule = sharedData(EdgeBiosVerticle.SHARED_MODULE_RULE);
        ITblModule tblModule = rule.parse(serviceData.getMetadata());
        AppConfig appConfig = serviceData.getAppConfig();
        if (String.format("%s:%s", "com.nubeiot.edge.module", "installer").equals(tblModule.getServiceId())) {
            InstallerConfig installerConfig = new InstallerConfig();
            installerConfig.setRepoConfig(repoConfig);
            appConfig = IConfig.merge(new JsonObject().put(installerConfig.key(), installerConfig.toJson()),
                                      serviceData.getAppConfig(), AppConfig.class);
        }
        return rule.parse(dataDir, tblModule, appConfig).setPublishedBy("NubeIO");
    }

}
