package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.MockData;

public class ProtocolDispatcherVerticle extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Protocol_Dispatcher();
    }

    @Test
    public void test_get_protocols_404(TestContext context) {
        assertRestByClient(context, HttpMethod.POST, "/api/s/dispatcher", 404, new JsonObject(
            "{\"message\":\"Unsupported HTTP method POST in '/dispatcher'\",\"code\":\"SERVICE_NOT_FOUND\"}"));
    }

    @Test
    public void test_get_protocol_dispatcher_200(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/dispatcher/1", 200, new JsonObject(
            "{\"protocol\":\"BACNET\",\"address\":\"bacnet.dispatcher.network\",\"action\":\"CREATE\"," +
            "\"global\":false,\"id\":1,\"state\":\"ENABLED\",\"entity\":\"network\"}"));
    }

}
