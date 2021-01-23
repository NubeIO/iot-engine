package com.nubeiot.core.http.upload;

import org.junit.Ignore;
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
public class UploadDownloadServerTest extends HttpServerTestBase {

    @Rule
    public Timeout timeout = Timeout.seconds(TestHelper.TEST_TIMEOUT_SEC);

    @Override
    protected String httpConfigFile() { return "uploadDownload.json"; }

    @Test
    @Ignore
    //TODO fix it when implementing HTTP client. For test, replace TEST_TIMEOUT_SEC to `3000`
    public void test(TestContext context) {
        Async async = context.async();
        startServer(context, new HttpServerRouter());
    }

}
