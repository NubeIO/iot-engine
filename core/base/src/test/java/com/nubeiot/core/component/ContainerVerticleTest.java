package com.nubeiot.core.component;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.utils.mock.MockConfig;
import com.nubeiot.core.utils.mock.MockContainerVerticle;
import com.nubeiot.core.utils.mock.MockProvider;
import com.nubeiot.core.utils.mock.MockUnitVerticle;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@RunWith(VertxUnitRunner.class)
public class ContainerVerticleTest {

    private Vertx vertx;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("com.nubeiot")).setLevel(Level.TRACE);
    }

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
    }

    @Test
    public void test_contain_two_unit_vertical_having_same_type_should_deploy_only_one(TestContext context) {
        ContainerVerticle containerVerticle = new MockContainerVerticle();
        MockProvider providerFirst = new MockProvider();
        providerFirst.setUnitVerticle(new MockUnitVerticle());
        MockProvider providerSecond = new MockProvider();
        providerSecond.setUnitVerticle(new MockUnitVerticle());
        containerVerticle.addProvider(providerFirst);
        containerVerticle.addProvider(providerSecond);

        Async async = context.async();
        VertxHelper.deploy(vertx.getDelegate(), context, new DeploymentOptions(), containerVerticle, deployId -> {
            context.assertNotNull(deployId);
            TestHelper.testComplete(async);
            Assert.assertEquals(vertx.deploymentIDs().size(), 2);
        });
    }

    @Test
    public void test_contain_two_unit_vertical_having_different_type_should_deploy_both(TestContext context) {
        ContainerVerticle containerVerticle = new MockContainerVerticle();
        DummyProvider providerFirst = new DummyProvider();
        providerFirst.setUnitVerticle(new DummyUnitVerticle());
        MockProvider providerSecond = new MockProvider();
        providerSecond.setUnitVerticle(new MockUnitVerticle());
        containerVerticle.addProvider(providerFirst);
        containerVerticle.addProvider(providerSecond);

        Async async = context.async();
        VertxHelper.deploy(vertx.getDelegate(), context, new DeploymentOptions(), containerVerticle, deployId -> {
            context.assertNotNull(deployId);
            TestHelper.testComplete(async);
            Assert.assertEquals(vertx.deploymentIDs().size(), 3);
        });
    }

    @Test
    public void test_container_throw_exception_cannot_start(TestContext context) {
        MockContainerVerticle containerVerticle = new MockContainerVerticle(true);

        DummyProvider providerFirst = new DummyProvider();
        providerFirst.setUnitVerticle(new DummyUnitVerticle());
        containerVerticle.addProvider(providerFirst);

        Async async = context.async();
        VertxHelper.deployFailed(vertx.getDelegate(), context, new DeploymentOptions(), containerVerticle, deployId -> {
            TestHelper.testComplete(async);
            Assert.assertEquals(vertx.deploymentIDs().size(), 0);
        });
    }

    @Test
    public void test_unit_throw_exception_cannot_start(TestContext context) {
        MockContainerVerticle containerVerticle = new MockContainerVerticle();
        DummyProvider providerFirst = new DummyProvider();
        providerFirst.setUnitVerticle(new DummyUnitVerticle());
        MockProvider providerSecond = new MockProvider();
        providerSecond.setUnitVerticle(new MockUnitVerticle(true));
        containerVerticle.addProvider(providerFirst);
        containerVerticle.addProvider(providerSecond);

        Async async = context.async();
        VertxHelper.deployFailed(vertx.getDelegate(), context, new DeploymentOptions(), containerVerticle, deployId -> {
            TestHelper.testComplete(async);
            Assert.assertEquals(vertx.deploymentIDs().size(), 0);
        });
    }

    @After
    public void after(TestContext context) {
        vertx.close();
    }

    final class DummyUnitVerticle extends UnitVerticle<MockConfig, UnitContext> {

        public DummyUnitVerticle() {
            this(false);
        }

        public DummyUnitVerticle(boolean error) {
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
            logger.info("Starting Fake Unit Verticle...");
            super.start();
        }

    }


    final class DummyProvider implements UnitProvider<DummyUnitVerticle> {

        @Getter
        @Setter
        private DummyUnitVerticle unitVerticle;

        @Override
        public Class<DummyUnitVerticle> unitClass() { return DummyUnitVerticle.class; }

        @Override
        public DummyUnitVerticle get() { return unitVerticle; }

    }

}
