package com.nubeiot.core;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.http.ClientAuth;
import io.vertx.core.net.JksOptions;

public class EventbusConfigTest {

    @Test
    public void test_default() throws JSONException {
        NubeConfig.SystemConfig.EventBusConfig config = new NubeConfig.SystemConfig.EventBusConfig();
        NubeConfig.SystemConfig.EventBusConfig from = IConfig.from("{\"acceptBacklog\":-1,\"clientAuth\":\"NONE\"}",
                                                                   NubeConfig.SystemConfig.EventBusConfig.class);
        System.out.println("FROM: " + from.toJson().encode());
        System.out.println("====================");
        System.out.println("DEFAULT: " + config.toJson().encode());
        JSONAssert.assertEquals(from.toJson().encode(), config.toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_custom() throws JSONException {
        NubeConfig.SystemConfig.EventBusConfig config = new NubeConfig.SystemConfig.EventBusConfig();
        config.setClientAuth(ClientAuth.REQUIRED);
        config.setSsl(true);
        config.setKeyStoreOptions(new JksOptions().setPath("eventBusKeystore.jks").setPassword("nubesparkEventBus"));
        config.setTrustStoreOptions(new JksOptions().setPath("eventBusKeystore.jks").setPassword("nubesparkEventBus"));
        NubeConfig.SystemConfig.EventBusConfig from = IConfig.from(
                "{\"clientAuth\":\"REQUIRED\"," + "\"ssl\":true,\"keyStoreOptions\":{\"path" +
                "\":\"eventBusKeystore.jks\"," + "\"password\":\"nubesparkEventBus\"}," +
                "\"trustStoreOptions\":{\"path" + "\":\"eventBusKeystore.jks\"," +
                "\"password\":\"nubesparkEventBus\"}}", NubeConfig.SystemConfig.EventBusConfig.class);
        System.out.println("FROM: " + from.toJson().encode());
        System.out.println("====================");
        System.out.println("DEFAULT: " + config.toJson().encode());
        JSONAssert.assertEquals(from.toJson().encode(), config.toJson().encode(), JSONCompareMode.STRICT);
    }

}
