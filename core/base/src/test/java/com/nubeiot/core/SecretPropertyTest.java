package com.nubeiot.core;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;

public class SecretPropertyTest {

    @Test
    public void secret_property_serialization_test() {
        SecretProperty secretProperty = new SecretProperty("@secret.abc", "xx");
        Assert.assertNotNull(secretProperty.toJson());
        Assert.assertEquals(secretProperty.getRef(), "@secret.abc");
        Assert.assertEquals(secretProperty.serializeToJson().getString("value"), "******");
        Assert.assertEquals(secretProperty.serializeToJson().getString("ref"), "@secret.abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void secret_property_invalid_deserialization_test() {
        JsonObject input = new JsonObject().put("ref", "@secret.abc");
        JsonData.convert(input, SecretProperty.class);
    }

    @Test
    public void secret_property_deserialization_test() {
        JsonObject input = new JsonObject().put("ref", "@secret.abc").put("value", "xx");
        SecretProperty secretProperty = JsonData.convert(input, SecretProperty.class);
        Assert.assertEquals("@secret.abc", secretProperty.getRef());
        Assert.assertEquals("xx", secretProperty.getValue());
    }

}
