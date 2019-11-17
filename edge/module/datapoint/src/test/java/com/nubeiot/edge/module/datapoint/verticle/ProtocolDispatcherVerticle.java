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
    public void test_get_tag_by_point_and_id(TestContext context) {
        assertRestByClient(context, HttpMethod.POST, "/api/s/protocol-dispatcher", 410, new JsonObject(
            "{\"message\":\"Not found '/protocol-dispatcher' with HTTP method POST\",\"code\":\"NOT_FOUND\"}"));
    }

    @Test
    public void test_get_protocol_dispatcher(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/protocol-dispatcher/1", 200, new JsonObject(
            "{\"id\":1,\"protocol\":\"BACNET\",\"entity\":\"network\",\"address\":\"bacnet" +
            ".dispatcher.network\"}"));
    }

}
