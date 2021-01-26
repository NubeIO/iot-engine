package com.nubeiot.edge.connector.bacnet.discover;

import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import io.github.zero88.qwe.JsonHelper;
import io.github.zero88.qwe.dto.JsonData;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions.Fields;

public class DiscoverOptionsTest {

    @Test
    public void test_from_null() {
        final DiscoverOptions opt = DiscoverOptions.from(1000, (JsonObject) null);
        Assert.assertEquals(1000, opt.getTimeout());
        Assert.assertEquals(TimeUnit.MILLISECONDS, opt.getTimeUnit());
        Assert.assertFalse(opt.isDetail());
        Assert.assertFalse(opt.isForce());
    }

    @Test
    public void test_from_no_timeout() {
        final DiscoverOptions opt = DiscoverOptions.from(5000, (JsonObject) null);
        Assert.assertEquals(3000, opt.getTimeout());
        Assert.assertEquals(TimeUnit.MILLISECONDS, opt.getTimeUnit());
        Assert.assertFalse(opt.isDetail());
        Assert.assertFalse(opt.isForce());
    }

    @Test
    public void test_from_json_exceed_max_timeout() {
        final DiscoverOptions opt = DiscoverOptions.from(1000, new JsonObject().put(Fields.timeout, 5)
                                                                               .put(Fields.timeUnit, TimeUnit.SECONDS));
        Assert.assertEquals(1, opt.getTimeout());
        Assert.assertEquals(TimeUnit.SECONDS, opt.getTimeUnit());
        Assert.assertFalse(opt.isDetail());
        Assert.assertFalse(opt.isForce());
    }

    @Test
    public void test_from_json_not_exceed_max_timeout() {
        final DiscoverOptions opt = DiscoverOptions.from(5000, new JsonObject().put(Fields.timeout, 4));
        Assert.assertEquals(4, opt.getTimeout());
        Assert.assertEquals(TimeUnit.MILLISECONDS, opt.getTimeUnit());
        Assert.assertFalse(opt.isDetail());
        Assert.assertFalse(opt.isForce());
    }

    @Test
    public void test_from_json_not_exceed_max_timeout_with_timeUnit() {
        final JsonObject req = new JsonObject().put(Fields.timeout, 3000).put(Fields.timeUnit, TimeUnit.MILLISECONDS);
        final DiscoverOptions opt = DiscoverOptions.from(5000, req);
        Assert.assertEquals(3000, opt.getTimeout());
        Assert.assertEquals(TimeUnit.MILLISECONDS, opt.getTimeUnit());
        Assert.assertFalse(opt.isDetail());
        Assert.assertFalse(opt.isForce());
    }

    @Test
    public void test_from_json_exceed_max_timeout_with_timeUnit() {
        final JsonObject req = new JsonObject().put(Fields.timeout, 3000).put(Fields.timeUnit, TimeUnit.MILLISECONDS);
        final DiscoverOptions opt = DiscoverOptions.from(2000, req);
        Assert.assertEquals(2000, opt.getTimeout());
        Assert.assertEquals(TimeUnit.MILLISECONDS, opt.getTimeUnit());
        Assert.assertFalse(opt.isDetail());
        Assert.assertFalse(opt.isForce());
    }

    @Test
    public void test_from_json_with_detail_n_persist() {
        final JsonObject req = new JsonObject().put(Fields.detail, true).put(Fields.force, true);
        final DiscoverOptions opt = DiscoverOptions.from(2000, req);
        Assert.assertEquals(2000, opt.getTimeout());
        Assert.assertEquals(TimeUnit.MILLISECONDS, opt.getTimeUnit());
        Assert.assertTrue(opt.isDetail());
        Assert.assertTrue(opt.isForce());
    }

    @Test
    public void test_from_json_with_detail_n_persist_in_string() {
        final JsonObject req = new JsonObject().put(Fields.detail, "x").put(Fields.force, "true");
        final DiscoverOptions opt = DiscoverOptions.from(2000, req);
        Assert.assertEquals(2000, opt.getTimeout());
        Assert.assertEquals(TimeUnit.MILLISECONDS, opt.getTimeUnit());
        Assert.assertFalse(opt.isDetail());
        Assert.assertTrue(opt.isForce());
    }

    @Test
    public void test_serialize() throws JSONException {
        final JsonObject req = new JsonObject().put(Fields.detail, "x").put(Fields.force, "true");
        final DiscoverOptions opt = DiscoverOptions.from(2000, req);
        System.out.println(opt.toJson().encodePrettily());
        JsonHelper.assertJson(new JsonObject("{\"timeout\":2000,\"timeUnit\":\"MILLISECONDS\",\"detail\":false," +
                                             "\"duration\":\"PT15M\",\"minItem\":0,\"maxItem\":-1,\"force\":true}"),
                              opt.toJson());
    }

    @Test
    public void test_deserialize() {
        final DiscoverOptions opt = JsonData.from(
            new JsonObject("{\"timeout\":2000,\"timeUnit\":\"MILLISECONDS\",\"force\":true,\"detail\":false}"),
            DiscoverOptions.class);
        Assert.assertEquals(2000, opt.getTimeout());
        Assert.assertEquals(TimeUnit.MILLISECONDS, opt.getTimeUnit());
        Assert.assertFalse(opt.isDetail());
        Assert.assertTrue(opt.isForce());
    }

}
