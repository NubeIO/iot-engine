package com.nubeiot.edge.connector.bacnet.dto;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.edge.connector.bacnet.MockBACnetConfig;

public class LocalDeviceMetadataTest {

    @Test
    public void test_default() {
        LocalDeviceMetadata metadata = LocalDeviceMetadata.from(new MockBACnetConfig());
        Assert.assertEquals(10000, metadata.getMaxTimeoutInMS());
        Assert.assertEquals(1173, metadata.getVendorId());
        Assert.assertEquals("Nube iO Operations Pty Ltd", metadata.getVendorName());
        Assert.assertEquals("NubeIO-Edge28", metadata.getModelName());
    }

    @Test
    public void test_serialize() throws JSONException {
        LocalDeviceMetadata metadata = LocalDeviceMetadata.from(new MockBACnetConfig());
        JsonHelper.assertJson(new JsonObject("{\"vendorId\":1173,\"vendorName\":\"Nube iO Operations Pty Ltd\"," +
                                             "\"deviceNumber\":85084,\"modelName\":\"NubeIO-Edge28\"," +
                                             "\"objectName\":\"NubeIO-Edge28-85084\",\"slave\":true," +
                                             "\"maxTimeoutInMS\":10000}"), metadata.toJson(),
                              JsonHelper.ignore("objectName"), JsonHelper.ignore("deviceNumber"));
    }

    @Test
    public void test_deserialize() {
        LocalDeviceMetadata metadata = JsonData.from(
            "{\"vendorId\":1173,\"vendorName\":\"Nube iO Operations Pty Ltd\",\"deviceNumber\":85084," +
            "\"modelName\":\"NubeIO-Edge28\",\"objectName\":\"NubeIO-Edge28-85084\",\"slave\":true," +
            "\"maxTimeoutInMS\":10000}", LocalDeviceMetadata.class);
        Assert.assertEquals(10000, metadata.getMaxTimeoutInMS());
        Assert.assertEquals(1173, metadata.getVendorId());
        Assert.assertEquals("Nube iO Operations Pty Ltd", metadata.getVendorName());
        Assert.assertEquals("NubeIO-Edge28", metadata.getModelName());
        Assert.assertEquals(85084, metadata.getDeviceNumber());
        Assert.assertEquals("NubeIO-Edge28-85084", metadata.getObjectName());
    }

}
