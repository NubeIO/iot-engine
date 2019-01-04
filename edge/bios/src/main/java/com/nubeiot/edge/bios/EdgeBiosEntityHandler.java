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
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.loader.ModuleTypeFactory;
import com.nubeiot.edge.core.model.Tables;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;

public class EdgeBiosEntityHandler extends EdgeEntityHandler {

    protected EdgeBiosEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    public Single<EventMessage> initData() {
        JsonObject appConfig = this.verticle.getNubeConfig().getAppConfig().toJson();
        JsonObject repositoryCfg = setupMavenRepos(appConfig.getJsonObject("repository", new JsonObject()));
        boolean autoInstall = appConfig.getBoolean("auto_install", true);
        return this.isFreshInstall()
                   .map(isFresh -> startup(appConfig, repositoryCfg, autoInstall, isFresh))
                   .map(r -> EventMessage.success(EventAction.INIT, r));
    }

    private Single<JsonObject> startup(JsonObject appConfig, JsonObject repositoryCfg, boolean autoInstall,
                                       Boolean isFresh) {
        if (!isFresh) {
            return this.startupModules();
        }
        if (autoInstall) {
            return initInstaller(repositoryCfg, appConfig.getJsonObject("default_app", new JsonObject()));
        }
        return Single.just(new JsonObject());
    }

    private Single<JsonObject> initInstaller(JsonObject repositoryCfg, JsonObject appCfg) {
        JsonObject deployCfg = appCfg.getJsonObject(Tables.TBL_MODULE.DEPLOY_CONFIG.getName().toLowerCase());
        NubeConfig.AppConfig appConfig = IConfig.merge(repositoryCfg, deployCfg, NubeConfig.AppConfig.class);
        JsonObject module = ModuleTypeFactory.getDefault().serialize(appCfg, this.verticle.getModuleRule());
        ITblModule tblModule = new TblModule().setPublishedBy("NubeIO").fromJson(module)
                                              .setDeployConfig(appConfig.toJson());
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
