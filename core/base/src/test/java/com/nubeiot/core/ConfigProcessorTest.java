package com.nubeiot.core;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.Consumer;

import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper.OSHelper;
import com.nubeiot.core.TestHelper.SystemHelper;
import com.nubeiot.core.cluster.ClusterType;

@RunWith(VertxUnitRunner.class)
public class ConfigProcessorTest {

    private ConfigProcessor processor;
    private NubeConfig nubeConfig;

    @Before
    public void before() {
        Vertx vertx = Vertx.vertx();
        processor = new ConfigProcessor(vertx);

        String jsonInput = "{\"__system__\":{\"__eventBus__\":{\"clientAuth\":\"REQUIRED\",\"ssl\":true," +
                           "\"clustered\":true,\"keyStoreOptions\":{\"path\":\"eventBusKeystore.jks\"," +
                           "\"password\":\"nubesparkEventBus\"},\"trustStoreOptions\":{\"path\":\"eventBusKeystore" +
                           ".jks\",\"password\":\"nubesparkEventBus\"}},\"__cluster__\":{\"active\":true,\"ha\":true," +
                           "\"listenerAddress\":\"com.nubeiot.dashboard.connector.edge.cluster\"}}," +
                           "\"__app__\":{\"__http__\":{\"host\":\"0.0.0.0\",\"port\":8086,\"enabled\":true," +
                           "\"rootApi\":\"/api\", \"alpnVersions\": [ \"HTTP_2\", \"HTTP_1_1\" ]},\"api" +
                           ".name\":\"edge-connector\"}}";
        nubeConfig = IConfig.from(jsonInput, NubeConfig.class);
    }

    @Test
    public void test_environment_config_overridden_system_config() throws Exception {
        SystemHelper.setEnvironment(Collections.singletonMap("NUBEIO_APP_HTTP_HOST", "2.2.2.2"));
        System.setProperty("nubeio.app.http.host", "1.1.1.1");

        String value = processor.mergeEnvAndSys().get("nubeio.app.http.host").toString();
        Assert.assertEquals("2.2.2.2", value);
    }

    @Test
    public void test_not_have_default_and_provide_config() {
        Optional<NubeConfig> nubeConfig = this.processor.processAndOverride(NubeConfig.class, null, null);
        Assert.assertFalse(nubeConfig.isPresent());
    }

    @Test
    public void test_properties_that_not_exist_in_appconfig_should_add() {
        System.setProperty("nubeio.app.name", "thanh");
        System.setProperty("nubeio.app.http1.abc.def", "123");

        overrideConfigThenAssert(finalResult -> {
            Assert.assertEquals("thanh", finalResult.getAppConfig().get("name"));
            Assert.assertEquals("{abc={def=123.0}}", finalResult.getAppConfig().get("http1").toString());
        });
    }

    @Test
    public void test_invalid_type() {
        System.setProperty("nubeio.app.http", "123");

        overrideConfigThenAssert(finalResult -> {
            Assert.assertEquals("{host=0.0.0.0, port=8086, enabled=true, rootApi=/api, alpnVersions=[HTTP_2, HTTP_1_1]}", finalResult.getAppConfig().get("__http__").toString());
        });
    }

    @Test
    public void test_override_app_config() {
        System.setProperty("nubeio.app.http.port", "8088");
        System.setProperty("nubeio.app.http.host", "2.2.2.2");
        System.setProperty("nubeio.app.http.enabled", "false");
        System.setProperty("nubeio.app.http.rootApi", "/test");

        overrideConfigThenAssert(finalResult -> {
            String httpConfig = finalResult.getAppConfig().get("__http__").toString();
            Assert.assertEquals(httpConfig, "{host=2.2.2.2, port=8088, enabled=false, rootApi=/test, alpnVersions=[" +
                                            "HTTP_2, HTTP_1_1]}");
        });
    }

    @Test
    public void test_override_app_config_with_array() {
        System.setProperty("nubeio.app.http.port", "8088");
        System.setProperty("nubeio.app.http.host", "2.2.2.2");
        System.setProperty("nubeio.app.http.enabled", "false");
        System.setProperty("nubeio.app.http.rootApi", "/test");
        System.setProperty("nubeio.app.http.alpnVersions", "[HTTP_2,HTTP_1_2]");

        overrideConfigThenAssert(finalResult -> {
            String httpConfig = finalResult.getAppConfig().get("__http__").toString();
            Assert.assertEquals(httpConfig, "{host=2.2.2.2, port=8088, enabled=false, rootApi=/test, alpnVersions=[" +
                                            "HTTP_2, HTTP_1_2]}");
        });
    }

    @Test
    public void test_override_system_config() {
        System.setProperty("nubeio.system.cluster.active", "false");
        System.setProperty("nubeio.system.cluster.type", "ZOOKEEPER");
        System.setProperty("nubeio.system.eventBus.port", "6000");
        System.setProperty("nubeio.system.eventBus.clustered", "true");

        overrideConfigThenAssert(finalResult -> {
            Assert.assertEquals(6000, finalResult.getSystemConfig().getEventBusConfig().getOptions().getPort());
            Assert.assertTrue(finalResult.getSystemConfig().getEventBusConfig().getOptions().isClustered());
            Assert.assertEquals(ClusterType.ZOOKEEPER, finalResult.getSystemConfig().getClusterConfig().getType());
            Assert.assertFalse(finalResult.getSystemConfig().getClusterConfig().isActive());
        });
    }

    @Test
    public void test_override_deploy_config() {
        System.setProperty("nubeio.deploy.workerPoolSize", "50");
        System.setProperty("nubeio.deploy.maxWorkerExecuteTime", "70000000000");
        System.setProperty("nubeio.deploy.worker", "true");

        overrideConfigThenAssert(finalResult -> {
            try {
                assertEquals("{\"ha\":false,\"instances\":1,\"maxWorkerExecuteTime\":70000000000," +
                             "\"maxWorkerExecuteTimeUnit\":\"NANOSECONDS\",\"multiThreaded\":false," +
                             "\"worker\":true,\"workerPoolSize\":50}", finalResult.getDeployConfig().toJson().encode(),
                             JSONCompareMode.STRICT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void test_invalid_data_type_should_be_used_default_config() {
        System.setProperty("nubeio.system.cluster.active", "invalid_type");

        overrideConfigThenAssert(
            finalResult -> Assert.assertTrue(finalResult.getSystemConfig().getClusterConfig().isActive()));
    }

    @Test
    public void test_env_config_is_json_but_provide_config_is_primitive() {
        System.setProperty("nubeio.app.http.port", "8087");
        String jsonInput1 = "{\"__app__\":{\"__http__\":\"anyvalue\"}}";

        nubeConfig = IConfig.from(jsonInput1, NubeConfig.class);
        Optional<NubeConfig> finalResult = this.processor.processAndOverride(NubeConfig.class, nubeConfig.toJson(),
                                                                             null);
        Assert.assertTrue(finalResult.isPresent());
        Object httpConfig = finalResult.get().getAppConfig().get("__http__");
        Assert.assertNotNull(httpConfig);
        Assert.assertEquals(httpConfig.toString(),
                            "anyvalue");
    }

    @Test
    public void test_default_config_and_override_config() {
        System.setProperty("nubeio.app.http.port", "8087");
        String jsonInput1 = "{\"__app__\":{\"__http__\":{\"host\":\"0.0.0.0\",\"port\":8086,\"enabled\":false," +
                            "\"rootApi\":\"/api\", \"alpnVersions\": [ \"HTTP_2\", \"HTTP_1_1\" ]},\"api" +
                            ".name\":\"edge-connector\"}}";
        String jsonInput2 = "{\"__app__\":{\"__http__\":{\"host\":\"1.1.1.1\",\"port\":8086," +
                            "\"rootApi\":\"/api\", \"alpnVersions\": [ \"HTTP_3\", \"HTTP_1_1\" ]},\"api" +
                            ".name\":\"edge-connector\"}}";
        nubeConfig = IConfig.from(jsonInput1, NubeConfig.class);
        NubeConfig nubeConfig2 = IConfig.from(jsonInput2, NubeConfig.class);
        Optional<NubeConfig> finalResult = this.processor.processAndOverride(NubeConfig.class, nubeConfig.toJson(),
                                                                             nubeConfig2.toJson());
        Assert.assertTrue(finalResult.isPresent());
        Object httpConfig = finalResult.get().getAppConfig().get("__http__");
        Assert.assertNotNull(httpConfig);
        Assert.assertEquals(httpConfig.toString(),
                            "{host=1.1.1.1, port=8087, enabled=false, rootApi=/api, alpnVersions=[" +
                            "HTTP_3, HTTP_1_1]}");
    }

    @Test
    public void test_data_dir() {
        System.setProperty("nubeio.dataDir", OSHelper.getAbsolutePathByOs("test").toString());
        overrideConfigThenAssert(
            finalResult -> Assert.assertEquals(finalResult.getDataDir(), OSHelper.getAbsolutePathByOs("test")));
    }

    @Test
    public void test_double() {
        System.setProperty("nubeio.app.http.port", "8087.0");
        String jsonInput = "{\"__app__\":{\"__http__\":{\"port\":8086.0}}}";
        nubeConfig = IConfig.from(jsonInput, NubeConfig.class);
        Optional<NubeConfig> finalResult = this.processor.processAndOverride(NubeConfig.class, null,
                                                                             nubeConfig.toJson());
        Assert.assertTrue(finalResult.isPresent());
        Object httpConfig = finalResult.get().getAppConfig().get("__http__");
        Assert.assertNotNull(httpConfig);
        Assert.assertEquals(httpConfig.toString(), "{port=8087.0}");
    }

    @Test
    public void test_float() {
        System.setProperty("nubeio.app.http.port", String.valueOf(3.4e+038));
        String jsonInput = "{\"__app__\":{\"__http__\":{\"port\":8080}}}";
        nubeConfig = IConfig.from(jsonInput, NubeConfig.class);
        ((LinkedHashMap) nubeConfig.getAppConfig().get("__http__")).put("port", (float) 3.4e+028);
        Optional<NubeConfig> finalResult = this.processor.processAndOverride(NubeConfig.class, null,
                                                                             nubeConfig.toJson());
        Assert.assertTrue(finalResult.isPresent());
        Object httpConfig = finalResult.get().getAppConfig().get("__http__");
        Assert.assertNotNull(httpConfig);
        Assert.assertEquals(httpConfig.toString(), "{port=3.4E38}");
    }

    @Test
    public void test_json_array_of_json_object() {
        System.setProperty("nubeio.app.https.port", "8087");
        String jsonInput =
            "{\"__app__\":{\"__https__\": [{\"host\": \"2.2.2.2\", \"port\": 8088, \"enabled\": false, " +
            "\"rootApi\": \"/test\"},{\"host\": \"2.2.2.3\", \"port\": 8089, \"enabled\": true, " +
            "\"rootApi\": \"/test1\"}]}}";
        nubeConfig = IConfig.from(jsonInput, NubeConfig.class);
        Optional<NubeConfig> finalResult = this.processor.processAndOverride(NubeConfig.class, null,
                                                                             nubeConfig.toJson());
        Assert.assertTrue(finalResult.isPresent());
        Object httpsConfig = finalResult.get().getAppConfig().get("__https__");
        Assert.assertNotNull(httpsConfig);
        Assert.assertEquals(httpsConfig.toString(),
                            "[{host=2.2.2.2, port=8088, enabled=false, rootApi=/test}, {host=2.2.2.3, port=8089, " +
                            "enabled=true, rootApi=/test1}]");
    }

    @Test
    public void test_json_array_of_primitive() {
        System.setProperty("nubeio.app.https", "[abc1,def1]");
        String jsonInput = "{\"__app__\":{\"__https__\": [\"abc\", \"def\"]}}";
        nubeConfig = IConfig.from(jsonInput, NubeConfig.class);
        Optional<NubeConfig> finalResult = this.processor.processAndOverride(NubeConfig.class, null,
                                                                             nubeConfig.toJson());
        Assert.assertTrue(finalResult.isPresent());
        Object httpsConfig = finalResult.get().getAppConfig().get("__https__").toString();
        Assert.assertNotNull(httpsConfig);
        Assert.assertEquals(httpsConfig.toString(), "[abc1, def1]");
    }

    @Test
    public void test_json_array_of_primitive_not_update() {
        System.setProperty("nubeio.app.https.name", "[abc1,def1]");
        String jsonInput = "{\"__app__\":{\"__https__\": [\"abc\", \"def\"]}}";
        nubeConfig = IConfig.from(jsonInput, NubeConfig.class);
        Optional<NubeConfig> finalResult = this.processor.processAndOverride(NubeConfig.class, null,
                                                                             nubeConfig.toJson());
        Assert.assertTrue(finalResult.isPresent());
        Object httpsConfig = finalResult.get().getAppConfig().get("__https__");
        Assert.assertNotNull(httpsConfig);
        Assert.assertEquals(httpsConfig.toString(), "[abc, def]");
    }

    @Test
    public void test_json_array_of_json_object_1() {
        System.setProperty("nubeio.app.http.host.name", "[abc.net,def.net]");
        String jsonInput = "{\"__app__\":{\"__http__\":{\"host\":[{\"name\":\"abc.com\"}, {\"name\":\"def" +
                           ".com\"}]}}}";

        nubeConfig = IConfig.from(jsonInput, NubeConfig.class);
        Optional<NubeConfig> finalResult = this.processor.processAndOverride(NubeConfig.class, null,
                                                                             nubeConfig.toJson());
        Assert.assertTrue(finalResult.isPresent());
        Object httpConfig = finalResult.get().getAppConfig().get("__http__");
        Assert.assertNotNull(httpConfig);
        Assert.assertEquals(httpConfig.toString(), "{host=[{name=abc.com}, {name=def.com}]}");
    }

    @Test
    public void test_json_array_of_primitive_1() {
        String jsonInput = "{\"__app__\":{\"__http__\":{\"host\":[\"abc.com\",\"def.com\"]}}}";
        System.setProperty("nubeio.app.http.host.name", "[abc.net,def.net]");
        nubeConfig = IConfig.from(jsonInput, NubeConfig.class);
        Optional<NubeConfig> finalResult = this.processor.processAndOverride(NubeConfig.class, null,
                                                                             nubeConfig.toJson());
        Assert.assertTrue(finalResult.isPresent());
        Object httpConfig = finalResult.get().getAppConfig().get("__http__");
        Assert.assertNotNull(httpConfig);
        Assert.assertEquals(httpConfig.toString(), "{host=[abc.com, def.com]}");
    }

    private void overrideConfigThenAssert(Consumer<NubeConfig> configConsumer) {
        Optional<NubeConfig> result = processor.processAndOverride(NubeConfig.class, nubeConfig.toJson(), null);
        configConsumer.accept(result.get());
    }

    @After
    public void after() throws Exception {
        SystemHelper.cleanEnvironments();
        System.clearProperty("nubeio.app.http.host");
        System.clearProperty("nubeio.app.http.host.name");
        System.clearProperty("nubeio.app.http.alpnVersions");
        System.clearProperty("nubeio.app.http.port");
        System.clearProperty("nubeio.app.https.port");
        System.clearProperty("nubeio.app.http.enabled");
        System.clearProperty("nubeio.app.http.rootApi");
        System.clearProperty("nubeio.app.https");
        System.clearProperty("nubeio.dataDir");
    }

}
