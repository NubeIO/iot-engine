package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.MockData;

public class ApiTest extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Edge_Network();
    }

    @Test
    public void test() {
    }

}
