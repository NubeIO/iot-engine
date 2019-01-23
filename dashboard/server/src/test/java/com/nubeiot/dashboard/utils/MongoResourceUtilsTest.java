package com.nubeiot.dashboard.utils;

import static com.nubeiot.dashboard.utils.MongoResourceUtils.ABSOLUTE_PATH_SUFFIX;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.http.HttpScheme;
import com.nubeiot.core.http.RegisterScheme;

import io.vertx.core.json.JsonObject;

public class MongoResourceUtilsTest {

    @Test
    public void test_absPath() {
        new RegisterScheme().register(HttpScheme.HTTPS);
        JsonObject jsonObject = new JsonObject().put("image", "32sdf93k2j12lf043");
        MongoResourceUtils.putAbsPath("localhost:8085", jsonObject, "image", "xyz.jpeg");
        Assert.assertEquals(jsonObject.getString("image" + ABSOLUTE_PATH_SUFFIX), "https://localhost:8085/xyz.jpeg");
    }

}
