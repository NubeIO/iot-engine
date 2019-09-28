package com.nubeiot.edge.connector.bacnet;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.edge.connector.bacnet.dto.BACnetIP;
import com.nubeiot.edge.connector.bacnet.service.NetworkDiscovery;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@RunWith(VertxUnitRunner.class)
public class BACnetVerticleTest {

    private Vertx vertx;
    private EventController busClient;

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
        VertxHelper.deploy(vertx, context, options, verticle, event -> busClient = verticle.getEventController());
    }

    private NubeConfig getNubeConfig() {
        return IConfig.fromClasspath("testConfig.json", NubeConfig.class);
    }

    @After
    public void after(TestContext context) {
        this.vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void test(TestContext context) {
        Async async = context.async();
        final BACnetIP dockerIp = BACnetIP.builder().subnet("172.20.0.1/16").name("docker").build();
        busClient.request(DeliveryEvent.builder()
                                       .address(NetworkDiscovery.class.getName())
                                       .action(EventAction.DISCOVER)
                                       .addPayload(RequestData.builder()
                                                              .body(dockerIp.toJson())
                                                              .filter(new JsonObject().put("timeout", 3))
                                                              .build())
                                       .build(), EventbusHelper.replyAsserter(context, async, new JsonObject()));
    }

}
