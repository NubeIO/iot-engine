package com.nubeiot.edge.bios;

import java.util.Map;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
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
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.loader.ModuleTypeFactory;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.eventbus.edge.EdgeEventBus;

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
        return EdgeEventBus.BIOS_DEPLOYMENT;
    }

    private Single<EventMessage> bootstrap(EventAction action) {
        JsonObject appCfg = IConfig.from(this.sharedDataFunc.apply(EdgeBiosVerticle.SHARED_APP_CFG), AppConfig.class)
                                   .toJson();
        logger.debug("Shared app configuration: {}", appCfg);
        JsonObject repoCfg = setupMavenRepos(appCfg.getJsonObject("repository", new JsonObject()));
        boolean auto = appCfg.getBoolean("auto_install", true);
        return this.isFreshInstall()
                   .flatMap(f -> startup(appCfg, repoCfg, auto, f).map(r -> EventMessage.success(action, r)));
    }

    private Single<JsonObject> startup(JsonObject appCfg, JsonObject repoCfg, boolean autoInstall, boolean isFresh) {
        if (!isFresh) {
            return this.startupModules();
        }
        if (autoInstall) {
            return initInstaller(repoCfg, appCfg.getJsonObject("default_app", new JsonObject()));
        }
        return Single.just(new JsonObject());
    }

    private Single<JsonObject> initInstaller(JsonObject repositoryCfg, JsonObject appCfg) {
        JsonObject installerDeployCfg = appCfg.getJsonObject("deploy_config", new JsonObject());
        AppConfig installerAppConfig = IConfig.merge(repositoryCfg, installerDeployCfg, NubeConfig.AppConfig.class);
        ModuleTypeRule rule = (ModuleTypeRule) this.sharedDataFunc.apply(EdgeBiosVerticle.SHARED_MODULE_RULE);
        JsonObject module = ModuleTypeFactory.getDefault().serialize(appCfg, rule);
        ITblModule tblModule = new TblModule().setPublishedBy("NubeIO")
                                              .fromJson(module)
                                              .setDeployConfig(installerAppConfig.toJson());
        return processDeploymentTransaction(tblModule, EventAction.INIT);
    }

    private JsonObject setupMavenRepos(JsonObject repositoryCfg) {
        logger.info("Setting up maven local and remote repo");
        String local = FileUtils.createFolder(repositoryCfg.getString("local"), "repository");
        JsonObject remotes = repositoryCfg.getJsonObject("remote", new JsonObject());
        remotes.stream().parallel().forEach(entry -> handleVerticleFactory(local, entry));
        return new JsonObject().put("remotes", remotes);
    }

    @SuppressWarnings("unchecked")
    private void handleVerticleFactory(String local, Map.Entry<String, Object> entry) {
        ModuleType type = ModuleTypeFactory.factory(entry.getKey());
        if (ModuleType.JAVA == type) {
            JsonObject remoteCfg = (JsonObject) entry.getValue();
            logger.info("Maven local repositories: {}", local);
            logger.info("Maven remote repositories: {}", remoteCfg);
            ResolverOptions resolver = new ResolverOptions().setRemoteRepositories(
                remoteCfg.getJsonArray("urls", new JsonArray()).getList()).setLocalRepository(local);
            vertx.registerVerticleFactory(new MavenVerticleFactory(resolver));
        }
    }

}
