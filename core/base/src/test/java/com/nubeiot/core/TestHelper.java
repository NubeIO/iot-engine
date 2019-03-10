package com.nubeiot.core;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.json.JSONException;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.reactivex.core.buffer.Buffer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public interface TestHelper {

    int TEST_TIMEOUT_SEC = 8;

    static int getRandomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    static void setup() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);
    }

    static void testComplete(Async async) {
        testComplete(async, "", null);
    }

    static void testComplete(Async async, String msgEvent, Handler<Void> completeAction) {
        System.out.println("Count:" + async.count());
        System.out.println(msgEvent);
        if (async.count() > 0) {
            async.countDown();
        }
        if (async.count() == 0 && !async.isCompleted()) {
            async.complete();
            if (Objects.nonNull(completeAction)) {
                completeAction.handle(null);
            }
        }
    }

    interface VertxHelper {

        static <T extends Verticle> void deploy(Vertx vertx, TestContext context, DeploymentOptions options, T verticle,
                                                Handler<String> handlerSuccess) {
            vertx.deployVerticle(verticle, options, context.asyncAssertSuccess(handlerSuccess));
        }

        static <T extends Verticle> T deploy(Vertx vertx, TestContext context, DeploymentOptions options, T verticle) {
            return deploy(vertx, context, options, verticle, TEST_TIMEOUT_SEC);
        }

        static <T extends Verticle> T deploy(Vertx vertx, TestContext context, DeploymentOptions options, T verticle,
                                             int timeout) {
            CountDownLatch latch = new CountDownLatch(1);
            vertx.deployVerticle(verticle, options, context.asyncAssertSuccess(id -> latch.countDown()));
            try {
                context.assertTrue(latch.await(timeout, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                context.fail(e);
            }
            return verticle;
        }

        static <T extends Verticle> void deployFailed(Vertx vertx, TestContext context, DeploymentOptions options,
                                                      T verticle, Handler<Throwable> errorHandler) {
            vertx.deployVerticle(verticle, options, context.asyncAssertFailure(errorHandler));
        }

    }


    interface EventbusHelper {

        static void assertConsumerData(Vertx vertx, Async async, String address, Consumer<Object> assertData) {
            assertConsumerData(vertx, async, address, assertData, null);
        }

        static void assertConsumerData(Vertx vertx, Async async, String address, Consumer<Object> assertData,
                                       Handler<Void> testCompleted) {
            MessageConsumer<Object> consumer = vertx.eventBus().consumer(address);
            consumer.handler(event -> {
                System.out.println("Received message from address: " + address);
                assertData.accept(event.body());
                consumer.unregister(v -> testComplete(async, "CONSUMER END", testCompleted));
            });
        }

    }


    interface OSHelper {

        String OS = System.getProperty("os.name").toLowerCase();

        static boolean isWin() {
            return OS.contains("win");
        }

        static boolean isMac() {
            return OS.contains("mac");
        }

        static boolean isUnix() {
            return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
        }

        static boolean isSolaris() {
            return OS.contains("sunos");
        }

        static Path getAbsolutePathByOs(String path) {
            if (isWin()) {
                return Paths.get("C:", path);
            }
            return Paths.get(path);
        }

    }


    interface JsonHelper {

        static CustomComparator comparator(Customization... customizations) {
            return new CustomComparator(JSONCompareMode.LENIENT, customizations);
        }

        static void assertJson(TestContext context, JsonObject expected, Buffer actual) {
            try {
                JSONAssert.assertEquals(expected.encode(), actual.toJsonObject().encode(), JSONCompareMode.STRICT);
            } catch (JSONException | AssertionError e) {
                context.fail(e);
            }
        }

        static void assertJson(TestContext context, Async async, JsonObject expected, JsonObject actual) {
            try {
                JSONAssert.assertEquals(expected.encode(), actual.encode(), JSONCompareMode.STRICT);
            } catch (JSONException | AssertionError e) {
                context.fail(e);
            } finally {
                testComplete(async);
            }
        }

        static void assertJson(TestContext context, Async async, JsonObject expected, JsonObject actual,
                               Customization... customizations) {
            try {
                JSONAssert.assertEquals(expected.encode(), actual.encode(), comparator(customizations));
            } catch (JSONException | AssertionError e) {
                context.fail(e);
            } finally {
                testComplete(async);
            }
        }

    }


    interface SystemHelper {

        @SuppressWarnings("unchecked")
        static void setEnvironment(Map<String, String> newEnv) throws Exception {
            try {
                Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
                Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
                theEnvironmentField.setAccessible(true);
                Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
                env.putAll(newEnv);
                Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField(
                    "theCaseInsensitiveEnvironment");
                theCaseInsensitiveEnvironmentField.setAccessible(true);
                Map<String, String> caseInsensitiveEnv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(
                    null);
                caseInsensitiveEnv.putAll(newEnv);
            } catch (NoSuchFieldException e) {
                Class<?>[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for (Class<?> cl : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newEnv);
                    }
                }
            }
        }

        static void cleanEnvironments() throws Exception {
            try {
                Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
                Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
                theEnvironmentField.setAccessible(true);
                Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
                env.clear();
                Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField(
                    "theCaseInsensitiveEnvironment");
                theCaseInsensitiveEnvironmentField.setAccessible(true);
                Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
                cienv.clear();
            } catch (NoSuchFieldException e) {
                Class<?>[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for (Class<?> cl : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                    }
                }
            }
        }

    }

}
