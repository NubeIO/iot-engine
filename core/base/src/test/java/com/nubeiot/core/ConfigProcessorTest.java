package com.nubeiot.core;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.util.Collections;
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
                           "\"rootApi\":\"/api\"},\"api.name\":\"edge-connector\"}}";
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
    public void test_override_app_config() {
        System.setProperty("nubeio.app.http.port", "8088");
        System.setProperty("nubeio.app.http.host", "2.2.2.2");
        System.setProperty("nubeio.app.http.enabled", "false");
        System.setProperty("nubeio.app.http.rootApi", "/test");

        overrideConfigThenAssert(finalResult -> {
            String httpConfig = finalResult.getAppConfig().get("__http__").toString();
            Assert.assertEquals(httpConfig, "{host=2.2.2.2, port=8088, enabled=false, rootApi=/test}");
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
                             "\"worker\":true,\"workerPoolSize\":50}",
                             finalResult.getDeployConfig().toJson().encode(),
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

    private void overrideConfigThenAssert(Consumer<NubeConfig> configConsumer) {
        Optional<NubeConfig> result = processor.processAndOverride(NubeConfig.class, nubeConfig, null);
        NubeConfig finalConfig = IConfig.merge(nubeConfig, result.get(), NubeConfig.class);
        configConsumer.accept(finalConfig);
    }

    @After
    public void after() throws Exception {
        SystemHelper.cleanEnvironments();
        System.clearProperty("nubeio.app.http.host");
    }

}
