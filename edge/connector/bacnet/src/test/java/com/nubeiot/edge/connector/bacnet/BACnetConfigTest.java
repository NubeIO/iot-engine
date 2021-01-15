package com.nubeiot.edge.connector.bacnet;

import static org.junit.Assert.assertFalse;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.Customization;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.utils.Configs;

public class BACnetConfigTest {

    @Test
    public void test_default() throws JSONException {
        BACnetConfig config = new BACnetConfig();
        assertFalse(config.isEnableSubscriber());
        System.out.println(config.toJson().encodePrettily());
        BACnetConfig fromFile = IConfig.from(Configs.loadJsonConfig("config.json"), BACnetConfig.class);
        JsonHelper.assertJson(config.toJson(), fromFile.toJson(),
                              Customization.customization("deviceId", (o1, o2) -> true),
                              Customization.customization("deviceName", (o1, o2) -> true));
    }

}
