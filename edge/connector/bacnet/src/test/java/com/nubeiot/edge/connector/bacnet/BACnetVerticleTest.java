package com.nubeiot.edge.connector.bacnet;

import java.util.Collection;
import java.util.Collections;

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
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventbusClient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RunWith(VertxUnitRunner.class)
public abstract class BACnetVerticleTest {

    protected Vertx vertx;
    protected EventbusClient busClient;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("com.serotonin.bacnet4j")).setLevel(Level.TRACE);
    }

    @Before
    public void before(TestContext context) {
        this.vertx = Vertx.vertx();
        final DeploymentOptions options = new DeploymentOptions().setConfig(getNubeConfig().toJson());
        final BACnetVerticle verticle = new BACnetVerticle();
        final Async async = context.async(2);
        VertxHelper.deploy(vertx, context, options, verticle, event -> {
            busClient = verticle.getEventbusClient()
                                .register("com.nubeiot.edge.connector.bacnet.readiness",
                                          createReadinessHandler(context, async));
            TestHelper.testComplete(async);
        });
    }

    @After
    public void after(TestContext context) {
        this.vertx.close(context.asyncAssertSuccess());
    }

    protected TestReadinessHandler createReadinessHandler(TestContext context, Async async) {
        return new TestReadinessHandler(context, async, new JsonObject("{\"total\":0}"));
    }

    protected NubeConfig getNubeConfig() {
        return IConfig.fromClasspath("testConfig.json", NubeConfig.class);
    }

    @RequiredArgsConstructor
    public static class TestReadinessHandler implements EventListener {

        @NonNull
        private final TestContext context;
        @NonNull
        private final Async async;
        @NonNull
        private final JsonObject expected;

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() {
            return Collections.singleton(EventAction.NOTIFY);
        }

        @EventContractor(action = EventAction.NOTIFY, returnType = boolean.class)
        public boolean receive(RequestData requestData) {
            JsonHelper.assertJson(context, async, expected, requestData.body());
            return true;
        }

    }

}
