package com.nubeiot.core.micro;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.utils.Configs;

public class MicroConfigTest {

    @Test
    public void test_default() throws JSONException {
        MicroConfig from = IConfig.from(Configs.loadJsonConfig("micro.json"), MicroConfig.class);
        MicroConfig def = new MicroConfig();
        JSONAssert.assertEquals(def.toJson().encode(), from.toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_parse_from_root() throws JSONException {
        NubeConfig from = IConfig.from(Configs.loadJsonConfig("micro.json"), NubeConfig.class);
        MicroConfig fromMicro = IConfig.from(Configs.loadJsonConfig("micro.json"), MicroConfig.class);
        JSONAssert.assertEquals(fromMicro.toJson().encode(),
                                from.getAppConfig().toJson().getJsonObject(MicroConfig.NAME).encode(),
                                JSONCompareMode.STRICT);
    }

    @Test
    public void test_parse_from_appConfig() throws JSONException {
        NubeConfig.AppConfig from = IConfig.from(Configs.loadJsonConfig("micro.json"), NubeConfig.AppConfig.class);
        MicroConfig microConfig = IConfig.from(from.toJson().getJsonObject(MicroConfig.NAME).encode(),
                                               MicroConfig.class);
        MicroConfig fromMicro = IConfig.from(Configs.loadJsonConfig("micro.json"), MicroConfig.class);
        JSONAssert.assertEquals(fromMicro.toJson().encode(), microConfig.toJson().encode(), JSONCompareMode.STRICT);
    }

}