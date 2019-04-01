package com.nubeiot.dashboard.connector.ditto;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;

public class DittoConfigTest {

    @Test
    public void testDittoConfig() throws JSONException {
        JsonObject appConfig = new JsonObject("{\"__ditto__\":{\"host\":\"infiniabiz.com\",\"port\":8080," +
                                              "\"username\":\"ditto\",\"password\":\"ditto\",\"policy\":true," +
                                              "\"prefix\":\"com.nubeio\"}," +
                                              "\"__micro__\":{\"__gateway__\":{\"enabled\":false}," +
                                              "\"__serviceDiscovery__\":{\"enabled\":true}," +
                                              "\"__localServiceDiscovery__\":{\"enabled\":false}," +
                                              "\"__circuitBreaker__\":{\"enabled\":false}}," +
                                              "\"__http__\":{\"port\":8088,\"__rest__\":{\"rootApi\":\"/api/ditto\"," +
                                              "\"__dynamic__\":{\"enabled\":true}}}}");
        DittoConfig dittoConfig = IConfig.from(appConfig, DittoConfig.class);
        JSONAssert.assertEquals(dittoConfig.toJson().encode(), appConfig.getJsonObject(DittoConfig.NAME).encode(),
                                false);
    }

}
