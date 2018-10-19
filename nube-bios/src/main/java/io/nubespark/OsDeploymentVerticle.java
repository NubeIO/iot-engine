package io.nubespark;

import java.util.Map;

import com.nubeio.iot.edge.EdgeVerticle;
import com.nubeio.iot.edge.loader.ModuleType;
import com.nubeio.iot.edge.loader.ModuleTypeFactory;
import com.nubeio.iot.edge.model.gen.tables.interfaces.ITblModule;
import com.nubeio.iot.edge.model.gen.tables.pojos.TblModule;
import com.nubeio.iot.share.DevRunner;
import com.nubeio.iot.share.enums.Status;
import com.nubeio.iot.share.event.EventType;
import com.nubeio.iot.share.utils.Configs;
import com.nubeio.iot.share.utils.FileUtils;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.maven.MavenVerticleFactory;
import io.vertx.maven.ResolverOptions;

public class OsDeploymentVerticle extends EdgeVerticle {

    public static void main(String[] args) {
        DevRunner.run("nube-bios/src/main/java/", OsDeploymentVerticle.class);
    }

    @Override
    protected String getDBName() {
        return "nube-bios";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Single<JsonObject> initData() {
        logger.info("Setup NubeIO Bios with config {}", getAppConfig().encode());
        JsonObject repositoryCfg = setupMavenRepos(getAppConfig().getJsonObject("repository", new JsonObject()));
        boolean autoInstall = getAppConfig().getBoolean("auto_install", true);
        return this.entityHandler.isFreshInstall().flatMap(isFresh -> {
            if (isFresh) {
                if (autoInstall) {
                    JsonObject defaultApp = getAppConfig().getJsonObject("default_app", new JsonObject());
                    return initApp(repositoryCfg, defaultApp);
                }
                return Single.just(new JsonObject().put("message", "nothing change").put("status", Status.SUCCESS));
            }
            return this.startupModules();
        });
    }

    private Single<JsonObject> initApp(JsonObject repositoryCfg, JsonObject appCfg) {
        ITblModule tblModule = new TblModule().setPublishedBy("NubeIO")
                                              .fromJson(ModuleTypeFactory.getDefault().serialize(appCfg))
                                              .setDeployConfigJson(
                                                      repositoryCfg.mergeIn(Configs.getApplicationCfg(appCfg)));
        return handleModule((TblModule) tblModule, EventType.INIT);
    }

    private JsonObject setupMavenRepos(JsonObject repositoryCfg) {
        logger.info("Setting up maven local and remote repo");
        String local = FileUtils.createFolder(repositoryCfg.getString("local"), ".nubeio", "repository");
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

}
