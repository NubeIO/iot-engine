package com.nubeiot.edge.connector.bacnet;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.utils.Configs;

@RunWith(MockitoJUnitRunner.class)
public class BACnetConfigTest {

    @Test
    public void test_default() throws JSONException {
        BACnetConfig config = new BACnetConfig();
        BACnetConfig fromFile = IConfig.fromClasspath("config.json", BACnetConfig.class);
        JsonHelper.assertJson(config.toJson(), fromFile.toJson(), JsonHelper.ignore("deviceId"),
                              JsonHelper.ignore("deviceName"));
    }

    @Test
    public void deserialize() {
        BACnetConfig fromFile = IConfig.from(Configs.loadJsonConfig("bacnetTestConfig.json"), BACnetConfig.class);
        Assert.assertNotNull(fromFile);
        Assert.assertEquals("NubeIOEdge28TEST", fromFile.getDeviceName());
        Assert.assertEquals(654321, fromFile.getDeviceId());

        Assert.assertNotNull(fromFile.getNetworks());
        Assert.assertEquals(2, fromFile.getNetworks().size());
        fromFile.getNetworks().toNetworks().forEach(ipConfig -> {
            Assert.assertNotNull(ipConfig);
            Assert.assertFalse(ipConfig.getName().isEmpty());
        });
    }

}
