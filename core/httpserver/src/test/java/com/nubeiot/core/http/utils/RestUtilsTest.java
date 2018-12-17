package com.nubeiot.core.http.utils;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.BaseHttpServerTest;
import com.nubeiot.core.http.HttpServerRouter;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class RestUtilsTest extends BaseHttpServerTest {

    @BeforeClass
    public static void beforeSuite() {
        BaseHttpServerTest.beforeSuite();
    }

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Test(timeout = DEFAULT_CONNECT_TIMEOUT, expected = InitializerError.class)
    public void test_not_yet_register() {
        startServer(new HttpServerRouter());
    }

}