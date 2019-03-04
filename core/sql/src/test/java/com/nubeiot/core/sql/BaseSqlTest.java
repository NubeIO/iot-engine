package com.nubeiot.core.sql;

import java.util.Objects;

import org.jooq.Catalog;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.sql.mock.manyschema.mock0.tables.TblSample_00;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.TblSample_01;
import com.nubeiot.core.utils.Configs;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class BaseSqlTest {

    static int TEST_TIMEOUT = 10;
    private Vertx vertx;
    private DeploymentOptions options;
    private String deployId;


    static class OneSchema {

        static final Catalog CATALOG = com.nubeiot.core.sql.mock.oneschema.DefaultCatalog.DEFAULT_CATALOG;

    }


    static class ManySchema {

        static final Catalog CATALOG = com.nubeiot.core.sql.mock.manyschema.DefaultCatalog.DEFAULT_CATALOG;
        static final TblSample_00 TBL_SAMPLE_00 = TblSample_00.TBL_SAMPLE_00;
        static final TblSample_01 TBL_SAMPLE_01 = TblSample_01.TBL_SAMPLE_01;

    }

    static void beforeSuite() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);
        ((Logger) LoggerFactory.getLogger("com.nubeiot")).setLevel(Level.DEBUG);
        ((Logger) LoggerFactory.getLogger("org.jooq")).setLevel(Level.DEBUG);
        ((Logger) LoggerFactory.getLogger("com.zaxxer.hikari")).setLevel(Level.DEBUG);
    }

    public void before(TestContext context) {
        SqlConfig sqlConfig = IConfig.from(Configs.loadJsonConfig("sql.json"), SqlConfig.class);
        sqlConfig.setDialect(getDialect());
        sqlConfig.getHikariConfig().setJdbcUrl(getJdbcUrl());
        vertx = Vertx.vertx().exceptionHandler(context.exceptionHandler());
        options = new DeploymentOptions().setConfig(sqlConfig.toJson());
    }

    abstract @NonNull String getJdbcUrl();

    SQLDialect getDialect() { return SQLDialect.H2; }

    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    void stopSQL(TestContext context) {
        System.out.println("Stop deployId: " + deployId);
        if (Objects.nonNull(deployId)) {
            vertx.undeploy(deployId, context.asyncAssertSuccess());
        }
    }

    <T extends EntityHandler> T startSQL(TestContext context, Catalog catalog, Class<T> handlerClass) {
        SQLWrapper<T> verticle = VertxHelper.deploy(vertx, context, options, new SQLWrapper<>(catalog, handlerClass),
                                                    TEST_TIMEOUT);
        deployId = verticle.deploymentID();
        return verticle.getContext().getEntityHandler();
    }

    <T extends EntityHandler> void startSQLFailed(TestContext context, Catalog catalog, Class<T> handlerClass,
                                                  Handler<Throwable> consumer) {
        VertxHelper.deployFailed(vertx, context, options, new SQLWrapper<>(catalog, handlerClass), consumer);
    }

    void assertSize(TestContext context, Async async, int expected, Result<? extends Record> rs) {
        try {
            context.assertEquals(expected, rs.size());
        } catch (AssertionError e) {
            context.fail(e);
        } finally {
            TestHelper.testComplete(async);
        }
    }

}
