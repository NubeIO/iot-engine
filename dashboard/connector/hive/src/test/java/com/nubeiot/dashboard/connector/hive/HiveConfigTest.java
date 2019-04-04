package com.nubeiot.dashboard.connector.hive;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.nubeiot.core.IConfig;

import io.vertx.core.json.JsonObject;

public class HiveConfigTest {

    @Test
    public void testHiveConfig() throws JSONException {
        JsonObject jsonObject = new JsonObject(
            "{\"url\": \"jdbc:hive2://localhost:10000/filo_db\", \"driver_class\": " +
            "\"org.apache.hive.jdbc.HiveDriver\", \"user\": \"root\", \"password\": " + "\"root\"}");
        HiveConfig hiveConfig = IConfig.from(jsonObject, HiveConfig.class);
        JSONAssert.assertEquals(hiveConfig.toJson().encode(), new JsonObject(
            "{\"url\":\"jdbc:hive2://localhost:10000/filo_db" + "\",\"driver_class\":\"org.apache.hive.jdbc" +
            ".HiveDriver\",\"user\":\"root\"," + "\"password\":\"root\"}").encode(), false);
    }

}
