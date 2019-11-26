package com.nubeiot.edge.connector.bacnet;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

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
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.micro.MicroConfig;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.Microservice;
import com.nubeiot.core.micro.MicroserviceProvider;

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
        final BacnetConfig bacnetCfg = getBACnetConfig();
        final BACnetVerticle verticle = new BACnetVerticle();
        final Async async = context.async(getMicroConfig().isPresent() ? 4 : 2);
        VertxHelper.deploy(vertx, context, getDeploymentOptions(bacnetCfg), verticle, event -> {
            deployMicroservice(vertx, context, async);
            busClient = verticle.getEventbusClient()
                                .register(bacnetCfg.getReadinessAddress(), createReadinessHandler(context, async));
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

    protected BacnetConfig getBACnetConfig() {
        return IConfig.fromClasspath("testConfig.json", BacnetConfig.class);
    }

    protected Optional<MicroConfig> getMicroConfig() {
        return Optional.empty();
    }

    protected void deployMicroservice(Vertx vertx, TestContext context, Async async) {
        getMicroConfig().ifPresent(cfg -> {
            final Microservice verticle = new MicroserviceProvider().get();
            VertxHelper.deploy(vertx, context, getDeploymentOptions(cfg), verticle, event1 -> {
                registerMockGatewayService(context, async, verticle.getContext());
                TestHelper.testComplete(async);
            });
        });
    }

    protected void registerMockGatewayService(TestContext context, Async async, MicroContext microContext) {

    }

    private DeploymentOptions getDeploymentOptions(@NonNull IConfig cfg) {
        final NubeConfig nubeConfig = IConfig.from(
            new JsonObject().put(AppConfig.NAME, new JsonObject().put(cfg.key(), cfg.toJson())), NubeConfig.class);
        return new DeploymentOptions().setConfig(nubeConfig.toJson());
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
