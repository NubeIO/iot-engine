package com.nubeiot.edge.connector.bacnet;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.event.EventController;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@RunWith(VertxUnitRunner.class)
public abstract class BACnetVerticleTest {

    protected Vertx vertx;
    protected EventController busClient;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("com.serotonin.bacnet4j")).setLevel(Level.TRACE);
    }

    @Before
    public void before(TestContext context) {
        DeploymentOptions options = new DeploymentOptions().setConfig(getNubeConfig().toJson());
        this.vertx = Vertx.vertx();
        final BACnetVerticle verticle = new BACnetVerticle();
        final Async async = context.async();
        VertxHelper.deploy(vertx, context, options, verticle, event -> {
            busClient = verticle.getEventController();
            TestHelper.sleep(1000);
            TestHelper.testComplete(async);
        });
    }

    private NubeConfig getNubeConfig() {
        return IConfig.fromClasspath("testConfig.json", NubeConfig.class);
    }

    @After
    public void after(TestContext context) {
        this.vertx.close(context.asyncAssertSuccess());
    }

}
