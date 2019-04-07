package com.nubeiot.edge.bios;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.maven.MavenVerticleFactory;
import io.vertx.maven.ResolverOptions;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.InstallerConfig;
import com.nubeiot.edge.core.InstallerConfig.RemoteUrl;
import com.nubeiot.edge.core.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.core.InstallerConfig.RepositoryConfig.RemoteRepositoryConfig;
import com.nubeiot.edge.core.RequestedServiceData;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

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
        return EdgeInstallerEventBus.BIOS_DEPLOYMENT;
    }

    private Single<EventMessage> bootstrap(EventAction action) {
        InstallerConfig installerCfg = IConfig.from(this.sharedDataFunc.apply(EdgeBiosVerticle.SHARED_INSTALLER_CFG),
                                                    InstallerConfig.class);
        Path dataDir = FileUtils.toPath((String) this.sharedDataFunc.apply(EdgeBiosVerticle.SHARED_DATA_DIR));
        logger.debug("Shared app configuration: {}", installerCfg);
        setupServiceRepository(installerCfg.getRepoConfig());
        return this.isFreshInstall()
                   .flatMap(f -> startup(dataDir, installerCfg, f).map(r -> EventMessage.success(action, r)));
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

    //TODO Fix multiple apps
    private Single<JsonObject> initInstaller(Path dataDir, RepositoryConfig repoConfig,
                                             List<RequestedServiceData> builtinApps) {
        if (builtinApps.isEmpty()) {
            return Single.just(new JsonObject().put("success", true));
        }
        RequestedServiceData firstApp = builtinApps.get(0);
        ModuleTypeRule rule = (ModuleTypeRule) this.sharedDataFunc.apply(EdgeBiosVerticle.SHARED_MODULE_RULE);
        JsonObject module = ModuleType.getDefault().serialize(firstApp.getMetadata(), rule);
        ITblModule tblModule = new TblModule().fromJson(module);
        tblModule.setPublishedBy("NubeIO")
                 .setDeployConfig(computeNubeConfig(dataDir, repoConfig, firstApp, tblModule).toJson());
        return processDeploymentTransaction(tblModule, EventAction.INIT);
    }

    private NubeConfig computeNubeConfig(Path dataDir, RepositoryConfig repoConfig, RequestedServiceData firstApp,
                                         ITblModule tblModule) {
        InstallerConfig installerConfig = new InstallerConfig();
        installerConfig.setRepoConfig(repoConfig);
        JsonObject appCfg = new JsonObject().put(installerConfig.name(), installerConfig.toJson());
        AppConfig installerAppConfig = IConfig.merge(appCfg, firstApp.getAppConfig(), AppConfig.class);
        NubeConfig nubeConfig = new NubeConfig();
        nubeConfig.setDataDir(FileUtils.recomputeDataDir(dataDir, tblModule.getServiceId()));
        nubeConfig.setAppConfig(installerAppConfig);
        return nubeConfig;
    }

    private void setupServiceRepository(RepositoryConfig repositoryCfg) {
        logger.info("Setting up service local and remote repository");
        RemoteRepositoryConfig remoteConfig = repositoryCfg.getRemoteConfig();
        remoteConfig.getUrls()
                    .entrySet()
                    .stream()
                    .parallel()
                    .forEach(entry -> handleVerticleFactory(repositoryCfg.getLocal(), entry));
    }

    private void handleVerticleFactory(String local, Entry<ModuleType, List<RemoteUrl>> entry) {
        final ModuleType type = entry.getKey();
        if (ModuleType.JAVA == type) {
            List<RemoteUrl> remoteUrls = entry.getValue();
            String javaLocal = FileUtils.createFolder(local, type.name().toLowerCase(Locale.ENGLISH));
            logger.info("{} local repositories: {}", type, javaLocal);
            logger.info("{} remote repositories: {}", type, remoteUrls);
            ResolverOptions resolver = new ResolverOptions().setRemoteRepositories(
                remoteUrls.stream().map(RemoteUrl::getUrl).collect(Collectors.toList())).setLocalRepository(javaLocal);
            vertx.registerVerticleFactory(new MavenVerticleFactory(resolver));
        }
    }

}
