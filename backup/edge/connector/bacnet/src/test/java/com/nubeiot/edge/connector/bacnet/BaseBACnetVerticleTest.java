package com.nubeiot.edge.connector.bacnet;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.component.ReadinessAsserter;
import com.nubeiot.core.event.EventbusClient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public abstract class BaseBACnetVerticleTest {

    protected Vertx vertx;
    protected EventbusClient busClient;
    protected String bacnetVerticleDeployId;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("com.serotonin.bacnet4j")).setLevel(Level.TRACE);
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
        final NubeConfig nubeConfig = IConfig.from(
            new JsonObject().put(AppConfig.NAME, new JsonObject().put(cfg.key(), cfg.toJson())), NubeConfig.class);
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
