package com.nubeiot.edge.bios;

import java.util.Map;
import java.util.function.Supplier;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.ModuleEventHandler;
import com.nubeiot.edge.core.TransactionEventHandler;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.loader.ModuleTypeFactory;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.gen.Tables;
import com.nubeiot.edge.core.model.gen.tables.interfaces.ITblModule;
import com.nubeiot.edge.core.model.gen.tables.pojos.TblModule;
import com.nubeiot.eventbus.edge.EdgeEventBus;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.maven.MavenVerticleFactory;
import io.vertx.maven.ResolverOptions;

public final class OsDeploymentVerticle extends EdgeVerticle {

    @Override
    protected void registerEventBus() {
        EventController controller = new EventController(getVertx());
        controller.consume(EdgeEventBus.BIOS_INSTALLER, new ModuleEventHandler(this, EdgeEventBus.BIOS_INSTALLER));
        controller.consume(EdgeEventBus.BIOS_TRANSACTION,
                           new TransactionEventHandler(this, EdgeEventBus.BIOS_TRANSACTION));
    }

    @Override
    protected Single<JsonObject> initData() {
        JsonObject appConfig = getNubeConfig().getAppConfig().toJson();
        logger.info("Setup NubeIO Bios with config {}", appConfig);
        JsonObject repositoryCfg = setupMavenRepos(appConfig.getJsonObject("repository", new JsonObject()));
        boolean autoInstall = appConfig.getBoolean("auto_install", true);
        return this.entityHandler.isFreshInstall().flatMap(isFresh -> {
            if (isFresh) {
                if (autoInstall) {
                    return initApp(repositoryCfg, appConfig.getJsonObject("default_app", new JsonObject()));
                }
                return Single.just(new JsonObject().put("message", "nothing change").put("status", Status.SUCCESS));
            }
            return this.startupModules();
        });
    }

    private Single<JsonObject> initApp(JsonObject repositoryCfg, JsonObject appCfg) {
        JsonObject deployCfg = appCfg.getJsonObject(Tables.TBL_MODULE.DEPLOY_CONFIG_JSON.getName());
        NubeConfig.AppConfig appConfig = IConfig.merge(repositoryCfg, deployCfg, NubeConfig.AppConfig.class);
        ITblModule tblModule = new TblModule().setPublishedBy("NubeIO")
                                              .fromJson(ModuleTypeFactory.getDefault()
                                                                         .serialize(appCfg, this.getModuleRule()))
                                              .setDeployConfigJson(appConfig.toJson());
        return processDeploymentTransaction((TblModule) tblModule, EventAction.INIT);
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
            vertx.getDelegate().registerVerticleFactory(new MavenVerticleFactory(resolver));
        }
    }

    @Override
    protected Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return new BIOSModuleTypeRuleProvider();
    }

}
