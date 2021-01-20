package com.nubeiot.edge.connector.bacnet;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import io.github.zero88.msa.bp.BlueprintConfig;
import io.github.zero88.msa.bp.BlueprintConfig.AppConfig;
import io.github.zero88.msa.bp.IConfig;
import io.github.zero88.msa.bp.TestHelper;
import io.github.zero88.msa.bp.TestHelper.VertxHelper;
import io.github.zero88.msa.bp.event.EventbusClient;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public abstract class BaseBACnetVerticleTest {

    protected Vertx vertx;
    protected EventbusClient busClient;
    protected String bacnetVerticleDeployId;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        //        ((Logger) LoggerFactory.getLogger("com.serotonin.bacnet4j")).setLevel(Level.TRACE);
    }

    @Before
    public final void before(TestContext context) {
        this.vertx = Vertx.vertx();
        deployServices(context);
    }

    @After
    public final void after(TestContext context) {
        Async async = context.async();
        this.vertx.undeploy(bacnetVerticleDeployId, event -> {
            TestHelper.testComplete(async);
            this.vertx.close(context.asyncAssertSuccess());
        });
    }

    protected abstract ReadinessAsserter createReadinessHandler(TestContext context, Async async);

    protected abstract void deployServices(TestContext context);

    protected final DeploymentOptions createDeploymentOptions(@NonNull IConfig cfg) {
        final BlueprintConfig nubeConfig = IConfig.from(
            new JsonObject().put(AppConfig.NAME, new JsonObject().put(cfg.key(), cfg.toJson())), BlueprintConfig.class);
        return new DeploymentOptions().setConfig(nubeConfig.toJson());
    }

    protected BACnetConfig createBACnetConfig() {
        return IConfig.fromClasspath("testConfig.json", BACnetConfig.class);
    }

    protected BACnetVerticle deployBACnetVerticle(TestContext context, Async async) {
        final BACnetConfig bacnetCfg = createBACnetConfig();
        final DeploymentOptions options = createDeploymentOptions(bacnetCfg);
        final BACnetVerticle verticle = new BACnetVerticle();
        return VertxHelper.deploy(vertx, context, options, verticle, deployId -> {
            bacnetVerticleDeployId = deployId;
            busClient = verticle.getEventbusClient()
                                .register(bacnetCfg.getReadinessAddress(), createReadinessHandler(context, async));
        });
    }

}
