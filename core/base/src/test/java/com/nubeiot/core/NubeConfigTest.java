package com.nubeiot.core;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.cluster.ClusterType;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Configs;

import io.vertx.core.json.JsonObject;

public class NubeConfigTest {

    @Test
    public void test_default() throws JSONException {
        NubeConfig nubeConfig = new NubeConfig();
        System.out.println(nubeConfig.toJson().encode());
        NubeConfig fromFile = IConfig.from(Configs.loadJsonConfig("system-test.json"), NubeConfig.class);
        JSONAssert.assertEquals(nubeConfig.toJson().encode(), fromFile.toJson().encode(),
                                JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void test_deserialize_simple_root() {
        JsonObject jsonObject = new JsonObject();
        NubeConfig from = IConfig.from(jsonObject, NubeConfig.class);
        Assert.assertNotNull(from.getDataDir());
    }

    @Test(expected = NubeException.class)
    public void test_deserialize_error_decode() { IConfig.from("hello", NubeConfig.class); }

    @Test
    public void test_deserialize_plain_child() {
        String jsonStr = "{\"active\":false,\"ha\":false,\"name\":\"\"," +
                         "\"type\":\"HAZELCAST\",\"listenerAddress\":\"\",\"url\":\"\",\"file\":\"\",\"options\":{}}";
        NubeConfig.SystemConfig.ClusterConfig cfg = IConfig.from(jsonStr, NubeConfig.SystemConfig.ClusterConfig.class);
        Assert.assertNotNull(cfg);
        Assert.assertEquals(ClusterType.HAZELCAST, cfg.getType());
    }

    @Test
    public void test_deserialize_child_from_parent_lvl1() {
        String jsonStr = "{\"__cluster__\":{\"active\":false,\"ha\":false,\"name\":\"\"," +
                         "\"type\":\"HAZELCAST\",\"listenerAddress\":\"\",\"url\":\"\",\"file\":\"\",\"options\":{}}}";
        NubeConfig.SystemConfig.ClusterConfig cfg = IConfig.from(jsonStr, NubeConfig.SystemConfig.ClusterConfig.class);
        Assert.assertNotNull(cfg);
        Assert.assertEquals(ClusterType.HAZELCAST, cfg.getType());
    }

    @Test
    public void test_deserialize_system_config() {
        String jsonStr = "{\"__cluster__\":{\"active\":false,\"ha\":false,\"name\":\"\",\"type\":\"HAZELCAST\"," +
                         "\"listenerAddress\":\"\",\"url\":\"\",\"file\":\"\",\"options\":{}}," +
                         "\"__eventBus__\":{\"sendBufferSize\":-1,\"receiveBufferSize\":-1,\"trafficClass\":-1," +
                         "\"reuseAddress\":true,\"logActivity\":false,\"reusePort\":false,\"tcpNoDelay\":true," +
                         "\"tcpKeepAlive\":false,\"soLinger\":-1,\"usePooledBuffers\":false,\"idleTimeout\":0," +
                         "\"idleTimeoutUnit\":\"SECONDS\",\"ssl\":false,\"enabledCipherSuites\":[],\"crlPaths\":[]," +
                         "\"crlValues\":[],\"useAlpn\":false,\"enabledSecureTransportProtocols\":[\"TLSv1\",\"TLSv1" +
                         ".1\",\"TLSv1.2\"],\"tcpFastOpen\":false,\"tcpCork\":false,\"tcpQuickAck\":false," +
                         "\"clustered\":false,\"clusterPublicPort\":0,\"clusterPingInterval\":20000," +
                         "\"clusterPingReplyInterval\":20000,\"port\":0,\"host\":\"localhost\",\"acceptBacklog\":-1," +
                         "\"clientAuth\":\"NONE\",\"reconnectAttempts\":0,\"reconnectInterval\":1000," +
                         "\"connectTimeout\":60000,\"trustAll\":true},\"__micro__\":{}}";
        NubeConfig.SystemConfig cfg = IConfig.from(jsonStr, NubeConfig.SystemConfig.class);
        Assert.assertNotNull(cfg);
        Assert.assertNotNull(cfg.getClusterConfig());
        Assert.assertNotNull(cfg.getEventBusConfig());
        Assert.assertNotNull(cfg.getMicroConfig());
        Assert.assertEquals(0, cfg.getMicroConfig().size());
    }

    @Test
    public void test_deserialize_child_from_root() {
        String jsonStr =
                "{\"dataDir\": \"\", \"__system__\":{\"__cluster__\":{\"active\":false,\"ha\":false,\"name\":\"\"," +
                "\"type\":\"HAZELCAST\",\"listenerAddress\":\"\",\"url\":\"\",\"file\":\"\",\"options\":{}}}}";
        NubeConfig.SystemConfig.ClusterConfig cfg = IConfig.from(jsonStr, NubeConfig.SystemConfig.ClusterConfig.class);
        Assert.assertNotNull(cfg);
        Assert.assertEquals(ClusterType.HAZELCAST, cfg.getType());
    }

    @Test
    public void test_deserialize_appCfg_from_root() {
        String jsonStr = "{\"__system__\":{},\"__app__\":{\"http.port\":8085}}";
        NubeConfig cfg = IConfig.from(jsonStr, NubeConfig.class);
        Assert.assertNotNull(cfg);
        Assert.assertNotNull(cfg.getAppConfig());
        Assert.assertEquals(8085, cfg.getAppConfig().get("http.port"));
    }

    @Test
    public void test_deserialize_appCfg_directly() {
        String jsonStr = "{\"__system__\":{},\"__app__\":{\"http.port\":8085}}";
        NubeConfig.AppConfig cfg = IConfig.from(jsonStr, NubeConfig.AppConfig.class);
        Assert.assertNotNull(cfg);
        Assert.assertEquals(8085, cfg.get("http.port"));
    }

    @Test(expected = NubeException.class)
    public void test_deserialize_appCfg_invalid_json() {
        IConfig.from("{\"__system__\":{},\"__app__\":8085}}", NubeConfig.AppConfig.class);
    }

    @Test
    public void test_deserialize_appCfg_limitation() {
        NubeConfig.AppConfig from = IConfig.from(
                "{\"__system__\":{\"__cluster__\":{},\"__eventbus__\":{},\"__micro__\":{}}}",
                NubeConfig.AppConfig.class);
        Assert.assertNotNull(from);
    }

}