package com.nubeiot.core.http.dynamic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.http.HttpServerTestBase;

@RunWith(VertxUnitRunner.class)
public class DynamicHttpServerTest extends HttpServerTestBase {

    List<String> deployIds = new ArrayList<>();

    @BeforeClass
    public static void beforeSuite() { TestHelper.setup(); }

    @Before
    public void before(TestContext context) throws IOException { super.before(context); }

    @Override
    protected String httpConfigFile() { return "gateway.json"; }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Test
    public void test_dynamic_http_api(TestContext context) throws IOException {
        Async async = context.async();
        JsonObject config = overrideHttpPort(httpConfig.getPort());
        JsonObject serviceConfig = overrideHttpPort(TestHelper.getRandomPort());
        VertxHelper.deploy(vertx.getDelegate(), context, new DeploymentOptions().setConfig(config), new GatewayServer(),
                           deployId -> {
                               System.out.println("Gateway Deploy Id: " + deployId);
                               deployIds.add(deployId);
                           });
        VertxHelper.deploy(vertx.getDelegate(), context, new DeploymentOptions().setConfig(serviceConfig),
                           new HttpServiceServer(), deployId -> {
                System.out.println("Service Deploy Id: " + deployId);
                deployIds.add(deployId);
                assertRestByClient(context, HttpMethod.GET, "/api/s/rest/test", 200,
                                   new JsonObject().put("hello", "dynamic"));
                async.complete();
            });
    }

    private JsonObject overrideHttpPort(int port) {
        return new JsonObject().put("__app__", new JsonObject().put("__http__", new JsonObject().put("port", port)));
    }

}
