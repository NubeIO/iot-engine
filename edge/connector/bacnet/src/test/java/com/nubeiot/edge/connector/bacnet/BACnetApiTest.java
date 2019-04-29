package com.nubeiot.edge.connector.bacnet;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.dynamic.DynamicServiceTestBase;

//TODO temporary ignore
@Ignore
@RunWith(VertxUnitRunner.class)
public class BACnetApiTest extends DynamicServiceTestBase {

    @BeforeClass
    public static void beforeSuite() { TestHelper.setup(); }

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
        startGatewayAndService(context, new MockBACnetVerticle(),
                               new DeploymentOptions().setConfig(deployConfig(TestHelper.getRandomPort())));
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Test
    public void test_event_not_found(TestContext context) {
        assertRestByClient(context, HttpMethod.PUT, "/api/driver/bacnet/devices", 409,
                           new JsonObject().put("code", ErrorCode.STATE_ERROR)
                                           .put("message", "Unsupported event UPDATE"));
    }

    @Test
    public void test_get_list_success(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/driver/bacnet/devices", 200, new JsonObject());
    }

}
