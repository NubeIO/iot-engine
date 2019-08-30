package com.nubeiot.core.dto;

import org.json.JSONException;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;

public class SortTest {

    @Test
    public void test_from_request() throws JSONException {
        final Sort sort = Sort.from("abc,-def,+xyz,g1.abc,g2.def,-g2.xy");
        final JsonObject expected = sort.toJson();
        System.out.println(expected);
        final Sort convert = JsonData.convert(expected, Sort.class);
        JsonHelper.assertJson(expected, convert.toJson());
    }

}
