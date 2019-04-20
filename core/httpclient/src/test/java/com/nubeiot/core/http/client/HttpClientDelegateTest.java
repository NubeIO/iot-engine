package com.nubeiot.core.http.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.exceptions.TimeoutException;
import com.nubeiot.core.http.base.HostInfo;

@RunWith(VertxUnitRunner.class)
public class HttpClientDelegateTest {

    private Vertx vertx;
    private HttpClientConfig config;
    private HostInfo hostInfo;

    @BeforeClass
    public static void beforeClass() {
        TestHelper.setup();
    }

    @Before
    public void setup() {
        vertx = Vertx.vertx();
        config = new HttpClientConfig();
        hostInfo = HostInfo.builder().host("postman-echo.com").build();
    }

    @After
    public void teardown(TestContext context) {
        HttpClientRegistry.getInstance().clear();
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void test_get_success(TestContext context) {
        Async async = context.async();
        HttpClientDelegate client = HttpClientDelegate.create(vertx, config, hostInfo);
        client.execute("/get?foo1=bar1&foo2=bar2", HttpMethod.GET, null)
              .doFinally(() -> TestHelper.testComplete(async))
              .subscribe(resp -> {
                  System.out.println(resp.body());
                  System.out.println(resp.headers());
                  JSONAssert.assertEquals("{\"foo1\":\"bar1\",\"foo2\":\"bar2\"}",
                                          resp.body().getJsonObject("args").encode(), JSONCompareMode.STRICT);
              });
    }

    @Test
    public void test_connection_timeout(TestContext context) {
        Async async = context.async();
        config.getOptions().setConnectTimeout(2000).setIdleTimeout(1);
        HttpClientDelegate client = HttpClientDelegate.create(vertx, config, hostInfo);
        client.execute("/delay/5", HttpMethod.GET, null)
              .doFinally(() -> TestHelper.testComplete(async))
              .subscribe((responseData, throwable) -> context.assertTrue(throwable instanceof TimeoutException));
    }

    @Test
    public void test_not_found_shallow_error(TestContext context) {
        Async async = context.async();
        config.getOptions().setConnectTimeout(2000).setIdleTimeout(1);
        HttpClientDelegate client = HttpClientDelegate.create(vertx, config, hostInfo);
        client.execute("/xxx", HttpMethod.GET, null)
              .doFinally(() -> TestHelper.testComplete(async))
              .subscribe((responseData, throwable) -> {
                  context.assertEquals(404, responseData.getStatus().code());
                  context.assertNull(throwable);
              });
    }

    @Test
    public void test_not_found_throw_error(TestContext context) {
        Async async = context.async();
        config.getOptions().setConnectTimeout(2000).setIdleTimeout(1);
        HttpClientDelegate client = HttpClientDelegate.create(vertx, config, hostInfo);
        client.execute("/xxx", HttpMethod.GET, null, false)
              .doFinally(() -> TestHelper.testComplete(async))
              .subscribe((responseData, throwable) -> {
                  context.assertNull(responseData);
                  context.assertNotNull(throwable);
                  context.assertTrue(throwable instanceof NubeException);
                  context.assertEquals(ErrorCode.NOT_FOUND, ((NubeException) throwable).getErrorCode());
              });
    }

    @Test
    public void test_cache(TestContext context) throws InterruptedException {
        Async async = context.async(3);
        CountDownLatch latch = new CountDownLatch(2);
        context.assertTrue(HttpClientRegistry.getInstance().getHttpRegistries().isEmpty());
        HttpClientDelegate client = HttpClientDelegate.create(vertx, config, hostInfo);
        context.assertEquals(1, HttpClientRegistry.getInstance().getHttpRegistries().size());
        HttpClientDelegate.create(vertx, config, hostInfo);
        context.assertEquals(1, HttpClientRegistry.getInstance().getHttpRegistries().size());
        final HostInfo host2 = HostInfo.builder().host("echo.websocket.orgx").build();
        HttpClientDelegate client2 = HttpClientDelegate.create(vertx, config, host2);
        context.assertEquals(2, HttpClientRegistry.getInstance().getHttpRegistries().size());
        client.execute("/xxx", HttpMethod.GET, null).subscribe(data -> {
            TestHelper.testComplete(async);
            System.out.println(data.toJson());
            latch.countDown();
            client.close();
        });
        client2.execute("/echo", HttpMethod.GET, null).subscribe((r, t) -> {
            try {
                Thread.sleep(1000);
                context.assertNotNull(t);
                context.assertNull(HttpClientRegistry.getInstance().getHttpRegistries().get(host2));
            } finally {
                latch.countDown();
                TestHelper.testComplete(async);
            }
        });
        final boolean await = latch.await(TestHelper.TEST_TIMEOUT_SEC, TimeUnit.SECONDS);
        if (await) {
            TestHelper.testComplete(async);
            context.assertTrue(HttpClientRegistry.getInstance().getHttpRegistries().isEmpty());
        } else {
            context.fail("Timeout");
        }
    }

}
