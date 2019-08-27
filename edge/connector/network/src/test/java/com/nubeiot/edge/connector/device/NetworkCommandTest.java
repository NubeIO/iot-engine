package com.nubeiot.edge.connector.device;

import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.dto.JsonData;

public class NetworkCommandTest {

    @Test(expected = IllegalArgumentException.class)
    public void test_invalid_type() {
        JsonObject jsonObject = new JsonObject("{\"__network_app__\":{\"type\":\"TEST\"}}");
        NetworkAppConfig networkAppConfig = IConfig.from(jsonObject, NetworkAppConfig.class);
        JsonData.convert(networkAppConfig.toJson(), NetworkCommand.class);
    }

    @Test
    public void test_valid_type() {
        JsonObject jsonObject = new JsonObject("{\"__network_app__\":{\"type\":\"CONNMANCTL\"}}");
        NetworkAppConfig networkAppConfig = IConfig.from(jsonObject, NetworkAppConfig.class);
        JsonData.convert(networkAppConfig.toJson(), NetworkCommand.class);
    }

}
