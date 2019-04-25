package com.nubeiot.edge.module.gateway;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.RequestData;

@RunWith(VertxUnitRunner.class)
public class DriverRegistrationTest extends EdgeGatewayTestBase {

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
    }

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
        startEdgeGateway(context);
    }

    @Test
    public void test(TestContext context) {
        assertRestByClient(context, HttpMethod.POST, "/api/drivers/registration",
                           RequestData.builder().body(new JsonObject().put("port", 8081)).build(), 200,
                           new JsonObject());
    }

}
