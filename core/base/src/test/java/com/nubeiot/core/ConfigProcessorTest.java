package com.nubeiot.core;

import java.util.Collections;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.VertxUnitRunner;

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
    public void environment_config_overridden_system_config() {
        String value = processor.mergeEnvAndSys().get("nubeio.app.http.host").toString();
        Assert.assertEquals("2.2.2.2", value);
    }

    @Test
    public void not_have_default_and_provide_config() throws Exception {
        Optional<NubeConfig> nubeConfig = this.processor.processAndOverride(NubeConfig.class, null, null);

        Assert.assertFalse(nubeConfig.isPresent());
    }

    @Test
    public void override_system_config() throws Exception {
        System.setProperty("nubeio.app.name", "thanh");
        System.setProperty("nubeio.app.http.enabled", "false");
        System.setProperty("nubeio.app.http.port", "8088");
        System.setProperty("nubeio.app.http.port.abc.dfd", "8088");
        System.setProperty("nubeio.app.http.abc.def", "123");
        System.setProperty("nubeio.app.http1.abc.def", "123");
        System.setProperty("nubeio.system.cluster.active", "false");
        System.setProperty("nubeio.deploy.cluster.abc.def", "123");
        System.setProperty("nubeio.dataDir", "C:/test");
        String jsonInput = "{\"__system__\":{\"__eventBus__\":{\"clientAuth\":\"REQUIRED\",\"ssl\":true," +
                           "\"clustered\":true,\"keyStoreOptions\":{\"path\":\"eventBusKeystore.jks\"," +
                           "\"password\":\"nubesparkEventBus\"},\"trustStoreOptions\":{\"path\":\"eventBusKeystore" +
                           ".jks\",\"password\":\"nubesparkEventBus\"}},\"__cluster__\":{\"active\":true,\"ha\":true," +
                           "\"listenerAddress\":\"com.nubeiot.dashboard.connector.edge.cluster\"}}," +
                           "\"__app__\":{\"__http__\":{\"host\":\"0.0.0.0\",\"port\":8086,\"enabled\":true," +
                           "\"rootApi\":\"/api\"},\"api.name\":\"edge-connector\"}}";
        NubeConfig nubeConfig = IConfig.from(jsonInput, NubeConfig.class);
        System.out.println(nubeConfig.toJson());
        Optional<NubeConfig> result = processor.processAndOverride(NubeConfig.class, nubeConfig, null);
        System.out.println(nubeConfig.toJson());
        NubeConfig finalResult = IConfig.merge(nubeConfig, result.get(), NubeConfig.class);
        System.out.println(finalResult.toJson());
        //        Assert.assertEquals(finalResult.get("__http__").toString(),
        //                            "{host=2.2.2.2, port=8088.0, enabled=false, rootApi=/api}");

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
