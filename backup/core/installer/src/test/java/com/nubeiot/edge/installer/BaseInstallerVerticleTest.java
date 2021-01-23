package com.nubeiot.edge.installer;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.edge.installer.mock.MockInstallerVerticle;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public abstract class BaseInstallerVerticleTest {

    protected Vertx vertx;
    protected String sharedKey;
    protected EventbusClient eventClient;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("org.jooq")).setLevel(Level.INFO);
        ((Logger) LoggerFactory.getLogger("com.zaxxer.hikari")).setLevel(Level.INFO);
    }

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        String url = "jdbc:h2:mem:dbh2mem-" + UUID.randomUUID().toString();
        JsonObject cfg = new JsonObject(
            "{\"__app__\":{\"__sql__\":{\"dialect\": \"H2\",\"__hikari__\":{\"jdbcUrl\":\"" + url + "\"}}}}");
        final MockInstallerVerticle verticle = verticle();
        VertxHelper.deploy(vertx, context, new DeploymentOptions().setConfig(cfg), verticle, deployId -> {
            sharedKey = verticle.getSharedKey();
            eventClient = SharedDataDelegate.getEventController(vertx, sharedKey);
        });
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @NonNull
    protected abstract <T extends MockInstallerVerticle> T verticle();

}
