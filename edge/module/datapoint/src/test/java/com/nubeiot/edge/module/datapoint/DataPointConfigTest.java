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
import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.http.client.HttpClientConfig;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.DataPointConfig.BuiltinData;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;
import com.nubeiot.edge.module.datapoint.policy.CleanupPolicy;
import com.nubeiot.edge.module.datapoint.policy.NewestCleanupPolicy;
import com.nubeiot.edge.module.datapoint.policy.OldestCleanupPolicy;

public class DataPointConfigTest {

    @Test
    public void serialize_default_config() throws JSONException {
        final JsonObject expected = new JsonObject(
            "{\"__lowdb_migration__\":{\"enabled\":false},\"__data_scheduler__\":[{\"type\":\"PURGE_HISTORY_DATA\"," +
            "\"enabled\":false,\"label\":{\"label\":\"Purge point history data\"},\"trigger\":{\"type\":\"CRON\"," +
            "\"expression\":\"0 0 0 1/1 * ? *\",\"timezone\":\"Australia/Sydney\"},\"policy\":{\"type\":\"oldest\"," +
            "\"max_item\":100,\"group_by\":\"point_id\",\"duration\":\"PT720H\"}},{\"type\":\"SYNC_EDGE_INFO\"," +
            "\"label\":{\"label\":\"Sync edge information to cloud\"},\"enabled\":false," +
            "\"trigger\":{\"type\":\"CRON\",\"expression\":\"0 0 0 1/1 * ? *\",\"timezone\":\"Australia/Sydney\"}}," +
            "{\"type\":\"SYNC_POINT_DATA\",\"label\":{\"label\":\"Sync point data to cloud\"},\"enabled\":false," +
            "\"trigger\":{\"type\":\"CRON\",\"expression\":\"0 0 0 1/1 * ? *\",\"timezone\":\"Australia/Sydney\"}}," +
            "{\"type\":\"SYNC_POINT_SETTING\",\"label\":{\"label\":\"Sync point setting data to cloud\"}," +
            "\"enabled\":false,\"trigger\":{\"type\":\"CRON\",\"expression\":\"0 0 0 1/1 * ? *\"," +
            "\"timezone\":\"Australia/Sydney\"}}]}");
        final JsonObject def = DataPointConfig.def().toJson();
        JsonObject builtin = JsonData.tryParse(def.remove(BuiltinData.NAME)).toJson();
        JsonObject dataSync = JsonData.tryParse(def.remove(DataSyncConfig.NAME)).toJson();
        JsonHelper.assertJson(expected, def);
        JsonHelper.assertJson(BuiltinData.def().toJson(), builtin, JSONCompareMode.LENIENT);
        JsonHelper.assertJson(DataSyncConfig.def().toJson(), dataSync);
        final DataPointConfig from = IConfig.from(expected, DataPointConfig.class);
        JsonHelper.assertJson(expected, from.toJson());
        final DataPointConfig cp = IConfig.fromClasspath("config.json", DataPointConfig.class);
        expected.put(BuiltinData.NAME, builtin);
        expected.put(DataSyncConfig.NAME, dataSync);
        JsonHelper.assertJson(expected, cp.toJson(), JSONCompareMode.LENIENT);
        System.out.println(cp.toJson().encodePrettily());
    }

    @Test
    public void deserialize_no_data() throws JSONException {
        final JsonObject expected = new JsonObject("{\"__lowdb_migration__\":{\"enabled\":false}}");
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

    @Test
    public void deserialize_custom_config() throws JSONException {
        JsonObject builtin = BuiltinData.def()
                                        .toJson().put(EdgeMetadata.INSTANCE.singularKeyName(), MockData.EDGE.toJson());
        final HostInfo hostInfo = HostInfo.builder().host("abc").port(80).build();
        final HttpClientConfig httpCfg = HttpClientConfig.create("edge.datapoint", hostInfo);
        final DataSyncConfig syncConfig = new DataSyncConfig("XXX", true, null, httpCfg.toJson());
        final DataPointConfig config = new DataPointConfig();
        config.setBuiltinData(JsonData.from(builtin, BuiltinData.class));
        config.setDataSyncConfig(syncConfig);
        final DataPointConfig cp = IConfig.fromClasspath("config.json", DataPointConfig.class);
        final DataPointConfig merge = IConfig.merge(cp, config, DataPointConfig.class);

        Assert.assertFalse(merge.getLowdbMigration().isEnabled());
        Assert.assertTrue(merge.getDataSyncConfig().isEnabled());
        Assert.assertEquals("XXX", merge.getDataSyncConfig().getType());
        JsonHelper.assertJson(httpCfg.toJson(), merge.getDataSyncConfig().getClientConfig());
        JsonHelper.assertJson(JsonPojo.from(MockData.EDGE).toJson(), JsonData.tryParse(
            merge.getBuiltinData().toJson().remove(EdgeMetadata.INSTANCE.singularKeyName())).toJson());
        JsonHelper.assertJson(BuiltinData.def().toJson(), merge.getBuiltinData().toJson(), JSONCompareMode.LENIENT);
        JsonHelper.assertJson(syncConfig.toJson(), merge.getDataSyncConfig().toJson(), JSONCompareMode.LENIENT);
    }

}
