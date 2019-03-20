package com.nubeiot.core.http;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.exceptions.InitializerError;

@RunWith(VertxUnitRunner.class)
public class HttpUtilsTest extends HttpServerTestBase {

    @Rule
    public Timeout timeout = Timeout.seconds(TestHelper.TEST_TIMEOUT_SEC);

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
    }

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Test
    public void test_not_yet_register(TestContext context) {
        startServer(context, new HttpServerRouter(), t -> context.assertTrue(t instanceof InitializerError));
    }

}
