package com.nubeiot.core.micro;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.micro.MicroConfig.BackendConfig;
import com.nubeiot.core.micro.MicroConfig.LocalServiceDiscoveryConfig;
import com.nubeiot.core.micro.MicroConfig.ServiceDiscoveryConfig;

public class MicroConfigTest {

    @Test
    public void test_default() {
        MicroConfig def = new MicroConfig();
        Assert.assertTrue(def.getDiscoveryConfig().isEnabled());
        Assert.assertEquals(ServiceDiscoveryConfig.NUBE_ANNOUNCE_ADDRESS,
                            def.getDiscoveryConfig().getAnnounceAddress());
        Assert.assertEquals(ServiceDiscoveryConfig.NUBE_USAGE_ADDRESS, def.getDiscoveryConfig().getUsageAddress());
        Assert.assertFalse(def.getDiscoveryConfig().isLocal());
        Assert.assertTrue(def.getDiscoveryConfig().isAutoRegistrationOfImporters());

        Assert.assertFalse(def.getLocalDiscoveryConfig().isEnabled());
        Assert.assertEquals(LocalServiceDiscoveryConfig.NUBE_ANNOUNCE_LOCAL_ADDRESS,
                            def.getLocalDiscoveryConfig().getAnnounceAddress());
        Assert.assertEquals(LocalServiceDiscoveryConfig.DEFAULT_USAGE_LOCAL_ADDRESS,
                            def.getLocalDiscoveryConfig().getUsageAddress());
        Assert.assertTrue(def.getLocalDiscoveryConfig().isLocal());
        Assert.assertFalse(def.getLocalDiscoveryConfig().isAutoRegistrationOfImporters());

        Assert.assertFalse(def.getCircuitConfig().isEnabled());
        System.out.println(def.toJson());
    }

    @Test
    public void test_parse() throws JSONException {
        MicroConfig from = IConfig.fromClasspath("micro.json", MicroConfig.class);
        JSONAssert.assertEquals(new MicroConfig().toJson().encode(), from.toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_parse_from_root() throws JSONException {
        MicroConfig fromRoot = IConfig.from(IConfig.fromClasspath("micro.json", NubeConfig.class), MicroConfig.class);
        MicroConfig fromMicro = IConfig.fromClasspath("micro.json", MicroConfig.class);
        System.out.println(fromRoot.toJson());
        System.out.println(fromMicro.toJson());
        JSONAssert.assertEquals(fromRoot.toJson().encode(), fromMicro.toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_parse_from_appConfig() throws JSONException {
        MicroConfig fromApp = IConfig.from(IConfig.fromClasspath("micro.json", AppConfig.class), MicroConfig.class);
        MicroConfig fromMicro = IConfig.fromClasspath("micro.json", MicroConfig.class);
        System.out.println(fromApp.toJson());
        System.out.println(fromMicro.toJson());
        JSONAssert.assertEquals(fromMicro.toJson().encode(), fromApp.toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_reload_backend() {
        MicroConfig fromMicro = IConfig.fromClasspath("micro.json", MicroConfig.class);
        Assert.assertFalse(fromMicro.getDiscoveryConfig().isLocal());
        fromMicro.getDiscoveryConfig().reloadProperty();
        Assert.assertFalse(Boolean.valueOf(System.getProperty(BackendConfig.DEFAULT_SERVICE_DISCOVERY_BACKEND)));

        Assert.assertTrue(fromMicro.getLocalDiscoveryConfig().isLocal());
        fromMicro.getLocalDiscoveryConfig().reloadProperty();
        Assert.assertTrue(Boolean.valueOf(System.getProperty(BackendConfig.DEFAULT_SERVICE_DISCOVERY_BACKEND)));
    }

    @Test
    public void test_merge() throws JSONException {
        MicroConfig config = IConfig.fromClasspath("micro.json", MicroConfig.class)
                                    .merge(IConfig.from("{\"serviceName\": \"cookco\",\"__serviceDiscovery__" +
                                                        "\":{\"announceAddress\":\"x\"," +
                                                        "\"backendConfiguration\":{\"backend-name\":\"a\"," +
                                                        "\"local\":false,\"more\":\"test\"},\"usageAddress\":\"y\"}}",
                                                        MicroConfig.class));
        System.out.println(config.toJson().encodePrettily());
        Assert.assertEquals("cookco", config.getServiceName());
        Assert.assertEquals("x", config.getDiscoveryConfig().getAnnounceAddress());
        Assert.assertEquals("y", config.getDiscoveryConfig().getUsageAddress());
        JSONAssert.assertEquals("{\"backend-name\":\"a\"," + "\"local\":false, \"more\": \"test\"}",
                                config.getDiscoveryConfig().getBackendConfiguration().encode(), JSONCompareMode.STRICT);
    }

}
