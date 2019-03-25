package com.nubeiot.core;

import static junit.framework.TestCase.assertNull;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.TestHelper.SystemHelper;

@RunWith(VertxUnitRunner.class)
public class ConfigProcessorTest {

    private Vertx vertx;
    private ConfigProcessor processor;

    @Before
    public void before() throws Exception {
        vertx = Vertx.vertx();
        processor = new ConfigProcessor(vertx);
        SystemHelper.setEnvironment(Collections.singletonMap("NUBEIO_APP_HTTP_HOST", "2.2.2.2"));
        System.setProperty("nubeio.app.http.host", "1.1.1.1");
    }

    @Test
    public void get_config_from_environment() {
        Map<String, Object> result = ConfigProcessor.getConfigFromEnvironment(vertx);
        String value = result.get("nubeio.app.http.host").toString();
        Assert.assertEquals("2.2.2.2", value);
    }

    @Test
    public void get_config_from_system() {
        JsonObject result = ConfigProcessor.getConfigFromSystem(vertx);
        String value = result.getString("nubeio.app.http.host");
        Assert.assertEquals("1.1.1.1", value);
    }

    @Test
    public void environment_config_overridden_system_config() {
        processor.process();
        String value = processor.getResult().get("nubeio.app.http.host").toString();
        Assert.assertEquals("2.2.2.2", value);
    }

    @Test
    public void environment_config_overridden_system_config_and_json_config() {
        ConfigStoreOptions propertiesStore = new ConfigStoreOptions().setType("json")
            .setConfig(
                new JsonObject().put("nubeio.app.http.host",
                                     "2.2.2.2"));
        processor.getMappingOptions()
            .put(propertiesStore,
                 entries -> entries.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
        processor.process();
        String value = processor.getResult().get("nubeio.app.http.host").toString();
        Assert.assertEquals("2.2.2.2", value);
    }

    @Test
    public void not_have_environment_config_should_use_system_config() throws Exception {
        SystemHelper.cleanEnvironments();
        assertNull(System.getenv("NUBEIO_APP_HTTP_HOST"));

        ConfigStoreOptions propertiesStore = new ConfigStoreOptions().setType("json")
            .setConfig(
                new JsonObject().put("nubeio.app.http.host",
                                     "2.2.2.2"));
        processor.getMappingOptions()
            .put(propertiesStore,
                 entries -> entries.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
        processor.process();
        String value = processor.getResult().get("nubeio.app.http.host").toString();
        Assert.assertEquals("1.1.1.1", value);
    }

    @Test
    public void not_have_environment_config_and_system_config_should_use_json_config() throws Exception {
        cleanSystemAndEnvironmentVariable();
        ConfigStoreOptions propertiesStore = new ConfigStoreOptions().setType("json")
            .setConfig(
                new JsonObject().put("nubeio.app.http.host",
                                     "0.0.0.0"));
        processor.getMappingOptions()
            .put(propertiesStore,
                 entries -> entries.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
        processor.process();
        String value = processor.getResult().get("nubeio.app.http.host").toString();
        Assert.assertEquals("0.0.0.0", value);
    }

    @Test
    public void not_have_config_from_environment_system_and_json_should_use_default_config() throws Exception {
        cleanSystemAndEnvironmentVariable();
        processor.process();
        Assert.assertNotNull(processor.getResult());
    }

    @Test
    public void override_config() {
        System.setProperty("nubeio.app.http.port", "8088");
        System.setProperty("nubeio.app.http.enabled", "false");
        String jsonInput = "{\"__system__\":{\"__eventBus__\":{\"clientAuth\":\"REQUIRED\",\"ssl\":true," +
                           "\"clustered\":true,\"keyStoreOptions\":{\"path\":\"eventBusKeystore.jks\"," +
                           "\"password\":\"nubesparkEventBus\"},\"trustStoreOptions\":{\"path\":\"eventBusKeystore" +
                           ".jks\",\"password\":\"nubesparkEventBus\"}},\"__cluster__\":{\"active\":true,\"ha\":true," +
                           "\"listenerAddress\":\"com.nubeiot.dashboard.connector.edge.cluster\"}}," +
                           "\"__app__\":{\"__http__\":{\"host\":\"0.0.0.0\",\"port\":8086,\"enabled\":true," +
                           "\"rootApi\":\"/api\"},\"api.name\":\"edge-connector\"}}";
        AppConfig appConfig = IConfig.from(jsonInput, AppConfig.class);
        Optional<AppConfig> result = processor.processAndOverride(NubeConfig.AppConfig.class, appConfig);

        AppConfig finalResult = IConfig.merge(appConfig, result.get(), AppConfig.class);
        Assert.assertEquals(finalResult.get("__http__").toString(),
                            "{host=2.2.2.2, port=8088.0, enabled=false, rootApi=/api}");
        System.out.println(finalResult.toJson());
    }

    @After
    public void after() throws Exception {
        cleanSystemAndEnvironmentVariable();
    }

    private void cleanSystemAndEnvironmentVariable() throws Exception {
        SystemHelper.cleanEnvironments();
        System.clearProperty("nubeio.app.http.host");
    }

}
