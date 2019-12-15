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
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.component.ApplicationProbeHandler.ApplicationReadinessHandler;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.exceptions.ErrorData;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RunWith(VertxUnitRunner.class)
public abstract class BaseBACnetVerticleTest {

    protected Vertx vertx;
    protected EventbusClient busClient;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("com.serotonin.bacnet4j")).setLevel(Level.TRACE);
    }

    @Before
    public final void before(TestContext context) {
        this.vertx = Vertx.vertx();
        final BACnetConfig bacnetCfg = createBACnetConfig();
        deployServices(context, bacnetCfg, new BACnetVerticle());
    }

    @After
    public final void after(TestContext context) {
        this.vertx.close(context.asyncAssertSuccess());
    }

    protected abstract TestReadinessHandler createReadinessHandler(TestContext context, Async async);

    protected abstract void deployServices(TestContext context, BACnetConfig bacnetCfg, BACnetVerticle verticle);

    protected final DeploymentOptions createDeploymentOptions(@NonNull IConfig cfg) {
        final NubeConfig nubeConfig = IConfig.from(
            new JsonObject().put(AppConfig.NAME, new JsonObject().put(cfg.key(), cfg.toJson())), NubeConfig.class);
        return new DeploymentOptions().setConfig(nubeConfig.toJson());
    }

    protected BACnetConfig createBACnetConfig() {
        return IConfig.fromClasspath("testConfig.json", BACnetConfig.class);
    }

    @RequiredArgsConstructor
    public static class TestReadinessHandler implements ApplicationReadinessHandler {

        @NonNull
        private final TestContext context;
        @NonNull
        private final Async async;
        @NonNull
        private final JsonObject expected;

        @Override
        @EventContractor(action = EventAction.NOTIFY, returnType = boolean.class)
        public boolean success(RequestData requestData) {
            JsonHelper.assertJson(context, async, expected, requestData.body());
            return true;
        }

        @Override
        @EventContractor(action = EventAction.NOTIFY_ERROR, returnType = boolean.class)
        public boolean error(ErrorData error) {
            JsonHelper.assertJson(context, async, expected, error.toJson());
            return true;
        }

    }

}
