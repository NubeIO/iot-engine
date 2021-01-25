package com.nubeiot.edge.connector.bacnet.simulator;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.zero88.qwe.CarlConfig;
import io.github.zero88.qwe.IConfig;
import io.github.zero88.qwe.TestHelper;
import io.github.zero88.qwe.TestHelper.VertxHelper;
import io.github.zero88.qwe.protocol.network.Ipv4Network;
import io.github.zero88.qwe.protocol.network.UdpProtocol;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class BACnetSimulatorTest {

    private Vertx vertx;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        //        ((Logger) LoggerFactory.getLogger("com.serotonin.bacnet4j")).setLevel(Level.TRACE);
    }

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @Test
    public void startSuccess(TestContext context) {
        Async async = context.async(2);
        final UdpProtocol firstActiveIp = UdpProtocol.builder()
                                                     .ip(Ipv4Network.getFirstActiveIp())
                                                     .port(47808)
                                                     .canReusePort(true)
                                                     .build();
        final JsonObject localDevice = new JsonObject(
            "{\"vendorId\":1173,\"vendorName\":\"Nube iO Operations Pty Ltd\",\"deviceNumber\":222,\"slave\":false," +
            "\"modelName\":\"NubeIO-Edge28\",\"objectName\":\"NubeIOSimulator\",\"maxTimeoutInMS\":1000," +
            "\"discoverCompletionAddress\":\"com.nubeiot.edge.connector.bacnet.discover.complete\"}");
        final JsonObject expected = new JsonObject().put("network", firstActiveIp.toJson())
                                                    .put("remoteDevices", new JsonArray())
                                                    .put("localDevice", localDevice);
        final SimulatorCompletionAsserter handler = new SimulatorCompletionAsserter(context, async, expected);
        final DeploymentOptions options = new DeploymentOptions().setConfig(
            IConfig.fromClasspath("test-config.json", CarlConfig.class).toJson());
        deployThenWait(context, async, options, handler);
    }

    private void deployThenWait(TestContext context, Async async, DeploymentOptions options,
                                SimulatorCompletionAsserter asserter) {
        final BACnetSimulator verticle = new BACnetSimulator(asserter);
        VertxHelper.deploy(vertx, context, options, verticle, deployId -> {
            System.out.println("Deployed ID: " + deployId);
            context.assertEquals(1, vertx.deploymentIDs().size());
            TestHelper.testComplete(async);
        });
    }

}
