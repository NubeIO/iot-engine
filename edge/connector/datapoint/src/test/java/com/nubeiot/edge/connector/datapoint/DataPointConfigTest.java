package com.nubeiot.edge.connector.datapoint;

import java.time.Duration;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.edge.connector.datapoint.policy.CleanupPolicy;
import com.nubeiot.edge.connector.datapoint.policy.NewestCleanupPolicy;
import com.nubeiot.edge.connector.datapoint.policy.OldestCleanupPolicy;

public class DataPointConfigTest {

    @Test
    public void serialize_default_config() throws JSONException {
        final JsonObject expected = new JsonObject(
            "{\"lowdb_migration\":{\"enabled\":false},\"builtin_data\":{\"measure_unit\":[{\"type\":\"number\"}," +
            "{\"type\":\"percentage\",\"symbol\":\"%\"},{\"type\":\"voltage\",\"symbol\":\"V\"}," +
            "{\"type\":\"celsius\",\"symbol\":\"U+2103\"},{\"type\":\"bool\",\"possible_values\":{\"0.5\":[\"true\"," +
            "\"on\",\"start\",\"1\"],\"0.0\":[\"false\",\"off\",\"stop\",\"0\",\"null\"]}},{\"type\":\"dBm\"," +
            "\"symbol\":\"dBm\"},{\"type\":\"hPa\",\"symbol\":\"hPa\"},{\"type\":\"lux\",\"symbol\":\"lx\"}," +
            "{\"type\":\"kWh\",\"symbol\":\"kWh\"}]},\"__publisher__\":{\"type\":\"\",\"enabled\":false}," +
            "\"__cleanup_policy__\":{\"enabled\":true,\"process\":{\"address\":\"com.nubeiot.edge.connector.datapoint" +
            ".service.HistoryDataService\",\"pattern\":\"REQUEST_RESPONSE\",\"action\":\"BATCH_DELETE\"}," +
            "\"triggerModel\":{\"type\":\"CRON\",\"name\":\"historyData\",\"group\":\"cleanup\"," +
            "\"timezone\":\"Australia/Sydney\",\"expression\":\"0 0 0 ? * SUN *\"},\"policy\":{\"type\":\"oldest\"," +
            "\"max_item\":100,\"group_by\":\"point_id\",\"duration\":\"PT720H\"}}}");
        JsonHelper.assertJson(expected, DataPointConfig.def().toJson());
        final DataPointConfig from = IConfig.from(expected, DataPointConfig.class);
        JsonHelper.assertJson(expected, from.toJson());
    }

    @Test
    public void deserialize_no_data() throws JSONException {
        final JsonObject expected = new JsonObject("{\"lowdb_migration\":{\"enabled\":false}}");
        JsonHelper.assertJson(expected, new DataPointConfig().toJson());
    }

    @Test
    public void deserialize_has_data() throws JSONException {
        final JsonObject expected = new JsonObject("{\"lowdb_migration\":{\"enabled\":false}}");
        JsonHelper.assertJson(expected, new DataPointConfig().toJson());
    }

    @Test
    public void serialize_cleanupPolicy() throws JSONException {
        CleanupPolicy cleanupPolicy = new OldestCleanupPolicy(10, "xx", Duration.ofHours(1));
        JsonHelper.assertJson(
            new JsonObject("{\"type\":\"oldest\",\"max_item\":10,\"group_by\":\"xx\"," + "\"duration\":\"PT1H\"}"),
            cleanupPolicy.toJson());
    }

    @Test
    public void deserialize_cleanupPolicy() {
        NewestCleanupPolicy policy = JsonData.from(
            "{\"type\":\"newest\",\"max_item\":10,\"group_by\":\"xx\",\"duration\":\"PT1H\"}",
            NewestCleanupPolicy.class);
        Assert.assertEquals(NewestCleanupPolicy.TYPE, policy.type());
        Assert.assertEquals(10, policy.getMaxItem());
        Assert.assertEquals("xx", policy.getGroupBy());
        Assert.assertEquals(Duration.ofHours(1), policy.getDuration());
    }

}
