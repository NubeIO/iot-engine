package com.nubeiot.edge.connector.bacnet.simulator;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@RunWith(VertxUnitRunner.class)
public class BACnetSimulatorTest {

    private Vertx vertx;
    private BACnetSimulator verticle;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("com.serotonin.bacnet4j")).setLevel(Level.TRACE);
    }

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
        this.verticle = new BACnetSimulator();
    }

    @Test
    public void test(TestContext context) {
        Async async = context.async();
        final DeploymentOptions options = new DeploymentOptions().setConfig(
            IConfig.fromClasspath("config.json", NubeConfig.class).toJson());
        VertxHelper.deploy(vertx, context, options, verticle, deployId -> {
            System.out.println("Deployed ID: " + deployId);
            context.assertEquals(1, vertx.deploymentIDs().size());
            TestHelper.testComplete(async);
        });
    }

}
