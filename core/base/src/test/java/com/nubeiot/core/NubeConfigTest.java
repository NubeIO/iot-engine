package com.nubeiot.core;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.cluster.ClusterType;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Configs;
import com.nubeiot.core.utils.FileUtils;

public class NubeConfigTest {

    @Test
    public void test_default() throws JSONException {
        NubeConfig from = IConfig.from(Configs.loadJsonConfig("system.json"), NubeConfig.class);
        System.out.println(from.toJson());
        Assert.assertEquals(FileUtils.DEFAULT_DATADIR, from.getDataDir());
        Assert.assertNotNull(from.getSystemConfig());
        Assert.assertNotNull(from.getSystemConfig().getClusterConfig());
        JSONAssert.assertEquals("{\"active\":true,\"ha\":false,\"name\":\"nubeio-cluster\",\"type\":\"HAZELCAST\"," +
                                "\"listenerAddress\":\"\",\"url\":\"\",\"file\":\"\",\"options\":{}}",
                                from.getSystemConfig().getClusterConfig().toJson().encode(), JSONCompareMode.STRICT);
        Assert.assertNotNull(from.getSystemConfig().getEventBusConfig());
        JSONAssert.assertEquals("{\"acceptBacklog\":-1,\"clientAuth\":\"NONE\",\"clusterPingInterval\":20000," +
                                "\"clusterPingReplyInterval\":20000,\"clusterPublicPort\":-1,\"clustered\":true," +
                                "\"connectTimeout\":60000,\"crlPaths\":[],\"crlValues\":[]," +
                                "\"enabledCipherSuites\":[],\"enabledSecureTransportProtocols\":[\"TLSv1\",\"TLSv1" +
                                ".1\",\"TLSv1.2\"],\"host\":\"0.0.0.0\",\"idleTimeout\":0," +
                                "\"idleTimeoutUnit\":\"SECONDS\",\"logActivity\":false,\"port\":5000," +
                                "\"receiveBufferSize\":-1,\"reconnectAttempts\":0,\"reconnectInterval\":1000," +
                                "\"reuseAddress\":true,\"reusePort\":false,\"sendBufferSize\":-1,\"soLinger\":-1," +
                                "\"ssl\":false,\"tcpCork\":false,\"tcpFastOpen\":false,\"tcpKeepAlive\":false," +
                                "\"tcpNoDelay\":true,\"tcpQuickAck\":false,\"trafficClass\":-1,\"trustAll\":true," +
                                "\"useAlpn\":false,\"usePooledBuffers\":false}\n",
                                from.getSystemConfig().getEventBusConfig().toJson().encode(), JSONCompareMode.STRICT);
        Assert.assertNotNull(from.getDeployConfig());
        JSONAssert.assertEquals("{\"ha\":false,\"instances\":1,\"maxWorkerExecuteTime\":60000000000," +
                                "\"maxWorkerExecuteTimeUnit\":\"NANOSECONDS\",\"multiThreaded\":false," +
                                "\"worker\":false,\"workerPoolSize\":20}", from.getDeployConfig().toJson().encode(),
                                JSONCompareMode.STRICT);
        Assert.assertNotNull(from.getAppConfig());
        Assert.assertTrue(from.getAppConfig().isEmpty());
    }

    @Test
    public void test_init() {
        NubeConfig from = new NubeConfig();
        System.out.println(from.toJson());
        Assert.assertNotNull(from.getDataDir());
        Assert.assertNull(from.getSystemConfig());
        Assert.assertNotNull(from.getAppConfig());
        Assert.assertNotNull(from.getDeployConfig());
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
                         "\"connectTimeout\":60000,\"trustAll\":true}}";
        NubeConfig.SystemConfig cfg = IConfig.from(jsonStr, NubeConfig.SystemConfig.class);
        Assert.assertNotNull(cfg);
        Assert.assertNotNull(cfg.getClusterConfig());
        Assert.assertNotNull(cfg.getEventBusConfig());
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
    public void test_deserialize_appCfg_from_root() throws JSONException {
        String jsonStr = "{\"__system__\":{},\"__app__\":{\"http.port\":8085}}";
        NubeConfig cfg = IConfig.from(jsonStr, NubeConfig.class);
        Assert.assertNotNull(cfg);
        Assert.assertNotNull(cfg.getAppConfig());
        Assert.assertEquals(8085, cfg.getAppConfig().get("http.port"));
        JSONAssert.assertEquals("{\"http.port\":8085}", cfg.getAppConfig().toJson().encode(), JSONCompareMode.STRICT);
        Assert.assertNotNull(cfg.getSystemConfig());
        Assert.assertNotNull(cfg.getSystemConfig().getClusterConfig());
        Assert.assertNotNull(cfg.getSystemConfig().getEventBusConfig());
        Assert.assertNotNull(cfg.getDeployConfig());
        Assert.assertNotNull(cfg.getDataDir());
    }

    @Test
    public void test_deserialize_appCfg_directly() {
        String jsonStr = "{\"__system__\":{},\"__app__\":{\"http.port\":8085}}";
        NubeConfig.AppConfig cfg = IConfig.from(jsonStr, NubeConfig.AppConfig.class);
        Assert.assertNotNull(cfg);
        Assert.assertEquals(1, cfg.size());
        Assert.assertEquals(8085, cfg.get("http.port"));
    }

    @Test(expected = NubeException.class)
    public void test_deserialize_appCfg_invalid_json() {
        IConfig.from("{\"__system__\":{},\"__app__\":8085}}", NubeConfig.AppConfig.class);
    }

    @Test
    public void test_deserialize_appCfg_limitation() {
        NubeConfig.AppConfig from = IConfig.from(
            "{\"__system__\":{\"__cluster__\":{},\"__eventbus__\":{},\"__micro__\":{}}}", NubeConfig.AppConfig.class);
        Assert.assertNotNull(from);
    }

    @Test
    public void test_blank() throws JSONException {
        NubeConfig blank = NubeConfig.blank();
        Assert.assertNotNull(blank);
        Assert.assertNotNull(blank.getDataDir());
        Assert.assertNotNull(blank.getAppConfig());
        Assert.assertTrue(blank.getAppConfig().isEmpty());
        Assert.assertNotNull(blank.getDeployConfig());
        JSONAssert.assertEquals("{\"worker\":false,\"multiThreaded\":false,\"workerPoolSize\":20," +
                                "\"maxWorkerExecuteTime\":60000000000,\"ha\":false,\"instances\":1," +
                                "\"maxWorkerExecuteTimeUnit\":\"NANOSECONDS\"}",
                                blank.getDeployConfig().toJson().encode(), JSONCompareMode.STRICT);
        Assert.assertNull(blank.getSystemConfig());
    }

    @Test
    public void test_blank_with_app_cfg() throws JSONException {
        NubeConfig blank = NubeConfig.blank(new JsonObject().put("hello", 1));
        Assert.assertNotNull(blank);
        Assert.assertNotNull(blank.getDataDir());
        Assert.assertNotNull(blank.getAppConfig());
        Assert.assertEquals(1, blank.getAppConfig().size());
        Assert.assertEquals(1, blank.getAppConfig().get("hello"));
        Assert.assertNotNull(blank.getDeployConfig());
        JSONAssert.assertEquals("{\"worker\":false,\"multiThreaded\":false,\"workerPoolSize\":20," +
                                "\"maxWorkerExecuteTime\":60000000000,\"ha\":false,\"instances\":1," +
                                "\"maxWorkerExecuteTimeUnit\":\"NANOSECONDS\"}",
                                blank.getDeployConfig().toJson().encode(), JSONCompareMode.STRICT);
        Assert.assertNull(blank.getSystemConfig());
    }

    @Test
    public void test_merge_with_default() throws JSONException {
        NubeConfig nubeConfig = IConfig.from(Configs.loadJsonConfig("system.json"), NubeConfig.class);
        String jsonInput = "{\"__system__\":{\"__eventBus__\":{\"clientAuth\":\"REQUIRED\",\"ssl\":true," +
                           "\"clustered\":true,\"keyStoreOptions\":{\"path\":\"eventBusKeystore.jks\"," +
                           "\"password\":\"nubesparkEventBus\"},\"trustStoreOptions\":{\"path\":\"eventBusKeystore" +
                           ".jks\",\"password\":\"nubesparkEventBus\"}},\"__cluster__\":{\"active\":true,\"ha\":true," +
                           "\"listenerAddress\":\"com.nubeiot.dashboard.connector.edge.cluster\"}}," +
                           "\"__app__\":{\"__http__\":{\"host\":\"0.0.0.0\",\"port\":8086,\"enabled\":true," +
                           "\"rootApi\":\"/api\"},\"api.name\":\"edge-connector\"}}";
        NubeConfig input = IConfig.from(jsonInput, NubeConfig.class);
        Assert.assertEquals("0.0.0.0", input.getSystemConfig().getEventBusConfig().getOptions().getHost());
        Assert.assertEquals(5000, input.getSystemConfig().getEventBusConfig().getOptions().getPort());
        JsonObject mergeJson = nubeConfig.toJson().mergeIn(input.toJson(), true);
        JsonObject mergeToJson = nubeConfig.mergeToJson(input);

        JSONAssert.assertEquals(mergeJson.encode(), mergeToJson.encode(), JSONCompareMode.STRICT);
        NubeConfig merge = IConfig.from(mergeToJson, NubeConfig.class);
        JSONAssert.assertEquals(mergeJson.encode(), merge.toJson().encode(), JSONCompareMode.STRICT);
        NubeConfig merge1 = nubeConfig.merge(input);
        System.out.println(mergeJson.encodePrettily());
        System.out.println("===========================================");
        System.out.println(merge1.toJson().encodePrettily());
        JSONAssert.assertEquals(mergeJson.encode(), merge1.toJson().encode(), JSONCompareMode.STRICT);
    }

}
