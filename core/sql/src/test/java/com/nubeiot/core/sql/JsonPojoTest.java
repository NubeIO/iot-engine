package com.nubeiot.core.sql;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.mock.manyschema.mock0.tables.pojos.TblSample_00;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.pojos.TblSample_01;

public class JsonPojoTest {

    @Test
    public void test_ignore_null_1() throws JSONException {
        JsonObject from = JsonPojo.from(new TblSample_00().setId(1).setFBool(true).setFStr("hey")).toJson();
        System.out.println(from);
        JSONAssert.assertEquals("{\"id\":1,\"f_bool\":true,\"f_str\":\"hey\"}", from.encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_ignore_null_2() throws JSONException {
        JsonObject from = JsonPojo.from(new TblSample_01().setId(1)
                                                          .setFBool(false)
                                                          .setFStr("hola")
                                                          .setFValue(new JsonObject().put("key", "spanish"))).toJson();
        System.out.println(from);
        JSONAssert.assertEquals("{\"id\":1,\"f_bool\":false,\"f_str\":\"hola\",\"f_value\":{\"key\":\"spanish\"}}",
                                from.encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_ignore_null_3() throws JSONException {
        JsonObject from = JsonPojo.from(new TblSample_00().setId(1)
                                                          .setFBool(true)
                                                          .setFStr("hello")
                                                          .setFDate(null)
                                                          .setFValue(new JsonObject().put("key", "english"))).toJson();
        System.out.println(from);
        JSONAssert.assertEquals("{\"id\":1,\"f_bool\":true,\"f_str\":\"hello\",\"f_value\":{\"key\":\"english\"}}",
                                from.encode(), JSONCompareMode.STRICT);
    }

}
