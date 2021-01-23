package com.nubeiot.edge.connector.bacnet;

import org.json.JSONException;
import org.junit.Test;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.TestHelper.JsonHelper;

public class AbstractBACnetConfigTest {

    @Test
    public void test_default() throws JSONException {
        AbstractBACnetConfig config = new MockBACnetConfig().setDeviceId(111);
        AbstractBACnetConfig fromFile = IConfig.fromClasspath("bacnet-cfg.json", MockBACnetConfig.class)
                                               .setDeviceId(111);
        JsonHelper.assertJson(config.toJson(), fromFile.toJson());
    }

}
