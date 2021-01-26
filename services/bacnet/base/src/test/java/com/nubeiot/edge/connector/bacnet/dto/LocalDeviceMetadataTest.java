package com.nubeiot.edge.connector.bacnet.dto;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.zero88.qwe.JsonHelper;
import io.github.zero88.qwe.dto.JsonData;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.MockBACnetConfig;

public class LocalDeviceMetadataTest {

    @Test
    public void test_default() {
        LocalDeviceMetadata metadata = LocalDeviceMetadata.from(new MockBACnetConfig());
        Assertions.assertEquals(6000, metadata.getMaxTimeoutInMS());
        Assertions.assertEquals(1173, metadata.getVendorId());
        Assertions.assertEquals("Nube iO Operations Pty Ltd", metadata.getVendorName());
        Assertions.assertEquals("NubeIO-Edge28", metadata.getModelName());
    }

    @Test
    public void test_serialize() throws JSONException {
        LocalDeviceMetadata metadata = LocalDeviceMetadata.from(new MockBACnetConfig());
        JsonHelper.assertJson(new JsonObject("{\"vendorId\":1173,\"vendorName\":\"Nube iO Operations Pty Ltd\"," +
                                             "\"deviceNumber\":85084,\"modelName\":\"NubeIO-Edge28\"," +
                                             "\"objectName\":\"NubeIO-Edge28-85084\",\"slave\":true," +
                                             "\"maxTimeoutInMS\":6000}"), metadata.toJson(),
                              JsonHelper.ignore("objectName"), JsonHelper.ignore("deviceNumber"));
    }

    @Test
    public void test_deserialize() {
        LocalDeviceMetadata metadata = JsonData.from(
            "{\"vendorId\":1173,\"vendorName\":\"Nube iO Operations Pty Ltd\",\"deviceNumber\":85084," +
            "\"modelName\":\"NubeIO-Edge28\",\"objectName\":\"NubeIO-Edge28-85084\",\"slave\":true," +
            "\"maxTimeoutInMS\":10000}", LocalDeviceMetadata.class);
        Assertions.assertEquals(10000, metadata.getMaxTimeoutInMS());
        Assertions.assertEquals(1173, metadata.getVendorId());
        Assertions.assertEquals("Nube iO Operations Pty Ltd", metadata.getVendorName());
        Assertions.assertEquals("NubeIO-Edge28", metadata.getModelName());
        Assertions.assertEquals(85084, metadata.getDeviceNumber());
        Assertions.assertEquals("NubeIO-Edge28-85084", metadata.getObjectName());
    }

}
