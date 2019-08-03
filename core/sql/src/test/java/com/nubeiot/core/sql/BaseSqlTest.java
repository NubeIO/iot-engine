package com.nubeiot.core.sql;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import org.jooq.Catalog;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.slf4j.LoggerFactory;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.ReplyEventHandler;
import com.nubeiot.core.sql.mock.manyschema.mock0.tables.TblSample_00;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.TblSample_01;
import com.nubeiot.core.utils.Configs;
import com.nubeiot.core.utils.Reflections.ReflectionField;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class BaseSqlTest {

    private final String sharedKey = getClass().getName();
    private Vertx vertx;
    private DeploymentOptions options;
    private String deployId;

    static void beforeSuite() {
        TestHelper.setup();
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

    EventController controller() {
        return SharedDataDelegate.getEventController(vertx, sharedKey);
    }

    void stopSQL(TestContext context) {
        System.out.println("Stop deployId: " + deployId);
        if (Objects.nonNull(deployId)) {
            vertx.undeploy(deployId, context.asyncAssertSuccess());
        }
    }

    <T extends EntityHandler> T startSQL(TestContext context, Catalog catalog, Class<T> handlerClass) {
        SQLWrapper<T> v = VertxHelper.deploy(vertx, context, options,
                                             new SQLWrapper<>(catalog, handlerClass).registerSharedKey(sharedKey),
                                             TestHelper.TEST_TIMEOUT_SEC * 2);
        deployId = v.deploymentID();
        return v.getContext().getEntityHandler();
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

    static <T> void assertValue(@NonNull TestContext context, @NonNull Async async, @NonNull VertxPojo pojo,
                                @NonNull String field, @NonNull T expect) {
        try {
            context.assertNotNull(pojo);
            context.assertEquals(expect, ReflectionField.getFieldValue(pojo, pojo.getClass().getDeclaredField(field),
                                                                       expect.getClass()));
        } catch (AssertionError | NoSuchFieldException ex) {
            context.fail(ex);
        } finally {
            TestHelper.testComplete(async);
        }
    }

    void asserter(TestContext context, boolean isSuccess, JsonObject expected, String address, EventAction action,
                  RequestData reqData) {
        asserter(context, isSuccess, expected, address, action, reqData, new CountDownLatch(1));
    }

    void asserter(TestContext context, boolean isSuccess, JsonObject expected, String address, EventAction action,
                  RequestData reqData, CountDownLatch latch) {
        final Async async = context.async();
        controller().request(address, EventPattern.REQUEST_RESPONSE, EventMessage.initial(action, reqData),
                             ReplyEventHandler.builder().action(action).success(msg -> {
                                 System.out.println(msg.toJson().encode());
                                 context.assertEquals(isSuccess, msg.isSuccess());
                                 JsonHelper.assertJson(context, async, expected, isSuccess
                                                                                 ? Objects.requireNonNull(msg.getData())
                                                                                 : msg.getError().toJson());
                                 latch.countDown();
                             }).build());
    }

    static class OneSchema {

        static final Catalog CATALOG = com.nubeiot.core.sql.mock.oneschema.DefaultCatalog.DEFAULT_CATALOG;

    }


    static class ManySchema {

        static final Catalog CATALOG = com.nubeiot.core.sql.mock.manyschema.DefaultCatalog.DEFAULT_CATALOG;
        static final TblSample_00 TBL_SAMPLE_00 = TblSample_00.TBL_SAMPLE_00;
        static final TblSample_01 TBL_SAMPLE_01 = TblSample_01.TBL_SAMPLE_01;

    }

}
