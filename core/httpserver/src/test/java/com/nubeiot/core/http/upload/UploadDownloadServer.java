package com.nubeiot.core.http.upload;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.HttpServerTestBase;

@RunWith(VertxUnitRunner.class)
public class UploadDownloadServer extends HttpServerTestBase {

    @Rule
    public Timeout timeout = Timeout.seconds(5000);

    @BeforeClass
    public static void beforeSuite() { TestHelper.setup(); }

    @Before
    public void before(TestContext context) throws IOException { super.before(context); }

    @After
    public void after(TestContext context) { super.after(context); }

    @Override
    protected String httpConfigFile() { return "uploadDownload.json"; }

    @Test
    public void test(TestContext context) {
        Async async = context.async();
        startServer(context, new HttpServerRouter());
    }

}
