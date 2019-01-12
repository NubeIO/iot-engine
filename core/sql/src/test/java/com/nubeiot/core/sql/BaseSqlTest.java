package com.nubeiot.core.sql;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.jooq.Catalog;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.sql.mock.manyschema.mock0.tables.TblSample_00;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.TblSample_01;
import com.nubeiot.core.utils.Configs;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class BaseSqlTest {

    public static int TEST_TIMEOUT = 10;
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

    <T extends EntityHandler> T startSQL(Catalog catalog, Class<T> handlerClass, TestContext context)
        throws InterruptedException {
        return startSQL(catalog, handlerClass, context::fail);
    }

    <T extends EntityHandler> T startSQL(Catalog catalog, Class<T> handlerClass, Consumer<Throwable> consumer)
        throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> ref = new AtomicReference<>();
        SQLWrapper<T> verticle = new SQLWrapper<>(catalog, handlerClass);
        vertx.deployVerticle(verticle, options, event -> {
            latch.countDown();
            if (event.failed()) {
                deployId = null;
                consumer.accept(event.cause());
                return;
            }
            deployId = event.result();
            ref.set(verticle.getEntityHandler());
            System.out.println("Complete starting SQL with deployId: " + deployId);
        });
        System.out.println("Wait max " + TEST_TIMEOUT + "s : " + latch.await(TEST_TIMEOUT, TimeUnit.SECONDS));
        return ref.get();
    }

    void assertData(Async async, TestContext context, JsonObject expected, JsonObject actual) {
        try {
            JSONAssert.assertEquals(expected.encode(), actual.encode(), JSONCompareMode.STRICT);
            testComplete(async);
        } catch (JSONException | AssertionError e) {
            context.fail(e);
            testComplete(async);
        }
    }

    void assertSize(TestContext context, Async async, int expected, Result<? extends Record> rs) {
        try {
            context.assertEquals(expected, rs.size());
            testComplete(async);
        } catch (AssertionError e) {
            context.fail(e);
            testComplete(async);
        }
    }

    void testComplete(Async async) {
        System.out.println("Count:" + async.count());
        if (async.count() > 0) {
            async.countDown();
        }
        if (async.count() == 0 && !async.isCompleted()) {
            async.complete();
        }
    }

}
