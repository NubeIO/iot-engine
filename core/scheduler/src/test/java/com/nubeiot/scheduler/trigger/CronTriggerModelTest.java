package com.nubeiot.scheduler.trigger;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.quartz.TriggerKey;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.exceptions.NubeException;

public class CronTriggerModelTest {

    @Test(expected = NubeException.class)
    public void test_unknown_expr() {
        CronTriggerModel.builder().name("test").expr("abc").build();
    }

    @Test
    public void test_serialize() throws JSONException {
        CronTriggerModel build = CronTriggerModel.builder().name("test").expr("0 0 12 1/1 * ? *").tz("UTC").build();
        JsonObject entries = build.toJson();
        System.out.println(entries.encodePrettily());
        JSONAssert.assertEquals(
            "{\"type\":\"CRON\",\"name\":\"test\",\"group\":\"DEFAULT\",\"expression\":\"0 0 12 1/1 * ?" +
            " *\",\"timezone\":\"UTC\"}", entries.encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_deserialize() {
        CronTriggerModel from = (CronTriggerModel) JsonData.from(
            "{\"type\":\"CRON\",\"name\":\"test\",\"group\":\"DEFAULT\",\"expression\":\"0 0 12 1/1 * ? *\"," +
            "\"timezone\":\"UTC\"}", TriggerModel.class);
        Assert.assertEquals(TriggerType.CRON, from.type());
        Assert.assertEquals(TriggerKey.triggerKey("test"), from.getKey());
        Assert.assertEquals("UTC", from.getTimezone().getID());
        Assert.assertEquals("0 0 12 1/1 * ? *", from.getExpression().getCronExpression());
    }

}
