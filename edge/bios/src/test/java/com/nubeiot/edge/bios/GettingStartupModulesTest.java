package com.nubeiot.edge.bios;

import org.jooq.SQLDialect;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.NonNull;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.NubeConfig.DeployConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.sql.SqlConfig;
import com.nubeiot.core.statemachine.StateMachine;
import com.nubeiot.edge.core.EdgeVerticle;

@RunWith(VertxUnitRunner.class)
public class GettingStartupModulesTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    protected Vertx vertx;
    protected EdgeVerticle initDataVerticle = new MockBiosVerticle(MockInitDataEntityHandler.class);
    protected EdgeVerticle testVerticle = new MockBiosVerticle(MockEdgeEntityHandler.class);

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("com.nubeiot")).setLevel(Level.ERROR);
        StateMachine.init();
    }

    @Before
    public void before(TestContext context) {
        SqlConfig sqlConfig = new SqlConfig();
        sqlConfig.getHikariConfig().setJdbcUrl(getJdbcUrl());
        sqlConfig.setDialect(SQLDialect.H2);

        NubeConfig nubeConfig = new NubeConfig();
        nubeConfig.setDeployConfig(new DeployConfig());

        AppConfig appConfig = new AppConfig();
        appConfig.put("__sql__", sqlConfig.toJson());

        nubeConfig.setAppConfig(appConfig);
        DeploymentOptions options = new DeploymentOptions().setConfig(nubeConfig.toJson());
        this.vertx = Vertx.vertx();

        Async async = context.async(1);
        this.vertx.getDelegate().deployVerticle(this.initDataVerticle, options, context.asyncAssertSuccess(result -> {
            TestHelper.testComplete(async);
        }));
        async.awaitSuccess();
        System.out.print("Starting verticle 2");
        Async async1 = context.async(1);
        this.vertx.getDelegate().deployVerticle(this.testVerticle, options, context.asyncAssertSuccess(result -> {
            TestHelper.testComplete(async1);
        }));
        async1.awaitSuccess();

    }

    public void after(TestContext context) {
        //this.vertx.close(context.asyncAssertSuccess());
    }

    protected @NonNull String getJdbcUrl() {
        return "jdbc:h2:file:" + folder.getRoot().toPath().resolve("dbh2local").toString();
    }

    @Test
    public void test_create_success(TestContext context) {
        System.out.println(this.getJdbcUrl());
    }
}
