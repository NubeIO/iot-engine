package com.nubeiot.edge.module.datapoint;

import java.time.Duration;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.edge.module.datapoint.policy.CleanupPolicy;
import com.nubeiot.edge.module.datapoint.policy.NewestCleanupPolicy;
import com.nubeiot.edge.module.datapoint.policy.OldestCleanupPolicy;

public class DataPointConfigTest {

    @Test
    public void serialize_default_config() throws JSONException {
        final JsonObject expected = new JsonObject(
            "{\"lowdb_migration\":{\"enabled\":false},\"builtin_data\":{\"unit\":[{\"type\":\"number\"," +
            "\"category\":\"ALL\"},{\"type\":\"percentage\",\"category\":\"ALL\",\"symbol\":\"%\"}," +
            "{\"type\":\"bool\",\"category\":\"ALL\"},{\"type\":\"revolutions_per_minute\"," +
            "\"category\":\"ANGULAR_VELOCITY\",\"symbol\":\"rpm\"},{\"type\":\"radians_per_second\"," +
            "\"category\":\"ANGULAR_VELOCITY\",\"symbol\":\"rad/s\"},{\"type\":\"volt\"," +
            "\"category\":\"ELECTRIC_POTENTIAL\",\"symbol\":\"V\"},{\"type\":\"lux\",\"category\":\"ILLUMINATION\"," +
            "\"symbol\":\"lx\"},{\"type\":\"kilowatt_hour\",\"category\":\"POWER\",\"symbol\":\"kWh\"}," +
            "{\"type\":\"dBm\",\"category\":\"POWER\",\"symbol\":\"dBm\"},{\"type\":\"hectopascal\"," +
            "\"category\":\"PRESSURE\",\"symbol\":\"hPa\"},{\"type\":\"fahrenheit\",\"category\":\"TEMPERATURE\"," +
            "\"symbol\":\"°F\"},{\"type\":\"celsius\",\"category\":\"TEMPERATURE\",\"symbol\":\"°C\"}," +
            "{\"type\":\"meters_per_second\",\"category\":\"VELOCITY\",\"symbol\":\"m/s\"}," +
            "{\"type\":\"kilometers_per_hour\",\"category\":\"VELOCITY\",\"symbol\":\"km/h\"}," +
            "{\"type\":\"miles_per_hour\",\"category\":\"VELOCITY\",\"symbol\":\"mph\"}]}," +
            "\"__publisher__\":{\"type\":\"\",\"enabled\":false},\"__cleanup_policy__\":{\"enabled\":true," +
            "\"process\":{\"address\":\"com.nubeiot.edge.module.datapoint.service.HistoryDataService\"," +
            "\"pattern\":\"REQUEST_RESPONSE\",\"action\":\"BATCH_DELETE\"},\"triggerModel\":{\"type\":\"CRON\"," +
            "\"expression\":\"0 0 0 ? * SUN *\",\"timezone\":\"Australia/Sydney\",\"name\":\"historyData\"," +
            "\"group\":\"cleanup\"},\"policy\":{\"type\":\"oldest\",\"max_item\":100,\"group_by\":\"point_id\"," +
            "\"duration\":\"PT720H\"}}}");
        JsonHelper.assertJson(expected, DataPointConfig.def().toJson(), JSONCompareMode.LENIENT);
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
            new JsonObject("{\"type\":\"oldest\",\"max_item\":10,\"group_by\":\"xx\",\"duration\":\"PT1H\"}"),
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
