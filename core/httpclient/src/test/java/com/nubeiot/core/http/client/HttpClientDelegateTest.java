package com.nubeiot.core.http.client;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.exceptions.TimeoutException;

@RunWith(VertxUnitRunner.class)
public class HttpClientDelegateTest {

    private Vertx vertx;
    private HttpClientConfig config;

    @BeforeClass
    public static void beforeClass() {
        TestHelper.setup();
    }

    @Before
    public void setup() {
        vertx = Vertx.vertx();
        config = new HttpClientConfig(new HttpClientOptions().setDefaultHost("postman-echo.com"));
    }

    @After
    public void teardown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void test_get_success(TestContext context) {
        Async async = context.async();
        HttpClientDelegate client = HttpClientDelegate.create(vertx, config);
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
        HttpClientDelegate client = HttpClientDelegate.create(vertx, config);
        client.execute("/delay/5", HttpMethod.GET, null)
              .doFinally(() -> TestHelper.testComplete(async))
              .subscribe((responseData, throwable) -> context.assertTrue(throwable instanceof TimeoutException));
    }

    @Test
    public void test_not_found_shallow_error(TestContext context) {
        Async async = context.async();
        config.getOptions().setConnectTimeout(2000).setIdleTimeout(1);
        HttpClientDelegate client = HttpClientDelegate.create(vertx, config);
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
        HttpClientDelegate client = HttpClientDelegate.create(vertx, config);
        client.execute("/xxx", HttpMethod.GET, null, false)
              .doFinally(() -> TestHelper.testComplete(async))
              .subscribe((responseData, throwable) -> {
                  context.assertNull(responseData);
                  context.assertNotNull(throwable);
                  context.assertTrue(throwable instanceof NubeException);
                  context.assertEquals(ErrorCode.NOT_FOUND, ((NubeException) throwable).getErrorCode());
              });
    }

}
