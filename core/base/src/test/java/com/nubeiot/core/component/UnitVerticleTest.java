package com.nubeiot.core.component;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.shareddata.LocalMap;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.exceptions.NubeException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class UnitVerticleTest {

    private Vertx vertx;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("com.nubeiot")).setLevel(Level.TRACE);
    }

    @Before
    public void before(TestContext context) throws IOException {
        vertx = Vertx.vertx();
    }

    @Test
    public void not_have_config_file_should_deploy_success(TestContext context) {
        MockUnitVerticle unitVerticle = new MockUnitVerticle();
        Async async = context.async();
        VertxHelper.deploy(vertx.getDelegate(), context, new DeploymentOptions(), unitVerticle, deployId -> {
            context.assertNotNull(deployId);
            TestHelper.testComplete(async);
        });
    }

    @Test
    public void invalid_config_should_deploy_failed(TestContext context) {
        MockUnitVerticle unitVerticle = new MockUnitVerticle();
        Async async = context.async();
        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("xx", "yyy"));
        VertxHelper.deployFailed(vertx.getDelegate(), context, options, unitVerticle, t -> {
            TestHelper.testComplete(async);
            Assert.assertTrue(t instanceof NubeException);
            Assert.assertEquals(t.getMessage(), "Invalid config format");
        });
    }

    @Test
    public void test_register_shared_data(TestContext context) {
        MockUnitVerticle unitVerticle = new MockUnitVerticle();
        final String key = MockUnitVerticle.class.getName();
        unitVerticle.registerSharedData(key);

        Async async = context.async();
        DeploymentOptions options = new DeploymentOptions();
        VertxHelper.deploy(vertx.getDelegate(), context, options, unitVerticle, t -> {
            unitVerticle.getSharedData(key);
            TestHelper.testComplete(async);
        });
    }

    @After
    public void after(TestContext context) {
        vertx.close();
    }

    static class MockUnitVerticle extends UnitVerticle<MockConfig, UnitContext> {

        public MockUnitVerticle() {
            super(UnitContext.VOID);
        }

        @Override
        public @NonNull Class<MockConfig> configClass() {
            return MockConfig.class;
        }

        @Override
        public @NonNull String configFile() {
            return "config.json";
        }

        @Override
        public void start() {
            logger.info("Starting Mock Config Unit...");
            super.start();
        }

    }


    static class MockConfig implements IConfig {

        @Override
        public String name() {
            return "mock";
        }

        @Override
        public Class<? extends IConfig> parent() {
            return null;
        }

    }

}

