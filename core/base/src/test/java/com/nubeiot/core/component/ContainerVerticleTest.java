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
import com.nubeiot.core.utils.mock.MockProvider;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@RunWith(VertxUnitRunner.class)
public class ContainerVerticleTest {

    private Vertx vertx;
    private MockContainerVerticle containerVerticle;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("com.nubeiot")).setLevel(Level.TRACE);
    }

    @Before
    public void before() {
        vertx = Vertx.vertx();
        containerVerticle = new MockContainerVerticle();
    }

    @Test
    public void test_contain_two_unit_vertical_having_same_type_should_deploy_only_one(TestContext context) {
        addDummyUnit();
        addDummyUnit();

        Async async = context.async();
        VertxHelper.deploy(vertx.getDelegate(), context, new DeploymentOptions(), containerVerticle, deployId -> {
            context.assertNotNull(deployId);
            TestHelper.testComplete(async);
            Assert.assertEquals(2, vertx.deploymentIDs().size());
        });
    }

    @Test
    public void test_contain_two_unit_vertical_having_different_type_should_deploy_both(TestContext context) {
        addDummyUnit();
        addMockUnit();

        Async async = context.async();
        VertxHelper.deploy(vertx.getDelegate(), context, new DeploymentOptions(), containerVerticle, deployId -> {
            context.assertNotNull(deployId);
            TestHelper.testComplete(async);
            Assert.assertEquals(3, vertx.deploymentIDs().size());
        });
    }

    @Test
    public void test_container_throw_exception_cannot_start(TestContext context) {
        containerVerticle.setError(true);
        addDummyUnit();

        Async async = context.async();
        VertxHelper.deployFailed(vertx.getDelegate(), context, new DeploymentOptions(), containerVerticle, deployId -> {
            TestHelper.testComplete(async);
            Assert.assertEquals(0, vertx.deploymentIDs().size());
        });
    }

    @Test
    public void test_unit_throw_exception_cannot_start(TestContext context) {
        addDummyUnit();
        addMockUnitHavingException();

        Async async = context.async();
        VertxHelper.deployFailed(vertx.getDelegate(), context, new DeploymentOptions(), containerVerticle, deployId -> {
            TestHelper.testComplete(async);
            Assert.assertEquals(0, vertx.deploymentIDs().size());
        });
    }

    private void addMockUnit() {
        addMockUnit(false);
    }

    private void addMockUnitHavingException() {
        addMockUnit(true);
    }

    private void addMockUnit(boolean error) {
        MockProvider provider = new MockProvider();
        provider.setUnitVerticle(new MockUnitVerticle(error));
        containerVerticle.addProvider(provider);
    }

    private void addDummyUnit() {
        DummyProvider provider = new DummyProvider();
        provider.setUnitVerticle(new DummyUnitVerticle());
        containerVerticle.addProvider(provider);
    }

    @After
    public void after() {
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
