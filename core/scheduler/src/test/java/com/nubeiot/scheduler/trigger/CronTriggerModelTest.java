package com.nubeiot.scheduler.trigger;

import org.junit.Assert;
import org.junit.Test;
import org.quartz.TriggerKey;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.scheduler.trigger.TriggerModel.TriggerType;

public class CronTriggerModelTest {

    @Test
    public void test_serialize() {
        CronTriggerModel build = CronTriggerModel.builder().tz("UTC").expr("as").name("test").build();
        JsonObject entries = build.toJson();
        System.out.println(entries);
    }

    @Test
    public void test_deserialize() {
        CronTriggerModel from = JsonData.from(
            "{\"type\":\"CRON\",\"name\":\"test\",\"group\":\"DEFAULT\",\"expr\":\"as\",\"tz\":\"UTC\"}",
            CronTriggerModel.class);
        Assert.assertEquals(TriggerType.CRON, from.type());
        Assert.assertEquals(TriggerKey.triggerKey("test"), from.getKey());
        Assert.assertEquals("UTC", from.getTz());
        Assert.assertEquals("as", from.getExpr());
    }

}
