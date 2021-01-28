package com.nubeiot.edge.connector.bacnet;

import static org.junit.Assert.assertFalse;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.Customization;

import io.github.zero88.qwe.IConfig;
import io.github.zero88.qwe.JsonHelper;
import io.github.zero88.qwe.utils.Configs;

public class BACnetServiceConfigTest {

    @Test
    public void test_default() throws JSONException {
        BACnetServiceConfig config = new BACnetServiceConfig();
        assertFalse(config.isEnableSubscriber());
        System.out.println(config.toJson().encodePrettily());
        BACnetServiceConfig fromFile = IConfig.from(Configs.loadJsonConfig("bacnet.json"), BACnetServiceConfig.class);
        JsonHelper.assertJson(config.toJson(), fromFile.toJson(),
                              Customization.customization("deviceId", (o1, o2) -> true),
                              Customization.customization("deviceName", (o1, o2) -> true));
    }

}
