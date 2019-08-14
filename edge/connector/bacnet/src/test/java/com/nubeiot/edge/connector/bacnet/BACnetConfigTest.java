package com.nubeiot.edge.connector.bacnet;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.utils.Configs;

@RunWith(MockitoJUnitRunner.class)
public class BACnetConfigTest {

    @Test
    public void test_default() throws JSONException {
        BACnetConfig config = new BACnetConfig();
        BACnetConfig fromFile = IConfig.from(Configs.loadJsonConfig("bacnetTestConfig.json"), BACnetConfig.class);
        JSONAssert.assertEquals(fromFile.toJson().encode(), config.toJson().encode(), JSONCompareMode.LENIENT);
    }

    @Test
    public void deserialize() {
        BACnetConfig fromFile = IConfig.from(Configs.loadJsonConfig("bacnetTestConfig2.json"), BACnetConfig.class);
        Assert.assertNotNull(fromFile);
        Assert.assertEquals("NubeIOEdge28TEST", fromFile.getDeviceName());
        Assert.assertEquals(654321, fromFile.getDeviceId());

        Assert.assertNotNull(fromFile.getIpConfigs());
        Assert.assertEquals(2, fromFile.getIpConfigs().size());
        fromFile.getIpConfigs().forEach(ipConfig -> {
            Assert.assertNotNull(ipConfig);
            Assert.assertFalse(ipConfig.getName().isEmpty());
        });
    }

}
