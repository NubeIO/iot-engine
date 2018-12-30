package com.nubeiot.core;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.http.ClientAuth;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;

public class EventbusConfigTest {

    @Test
    public void test_default() throws JSONException {
        NubeConfig.SystemConfig.EventBusConfig config = new NubeConfig.SystemConfig.EventBusConfig();
        System.out.println("DEFAULT: " + config.toJson().encode());
        System.out.println("====================");
        NubeConfig.SystemConfig.EventBusConfig from = IConfig.from("{\"acceptBacklog\":-1,\"clientAuth\":\"NONE\"}",
                                                                   NubeConfig.SystemConfig.EventBusConfig.class);
        System.out.println("FROM: " + from.toJson().encode());
        JSONAssert.assertEquals(from.toJson().encode(), config.toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_custom() throws JSONException {
        NubeConfig.SystemConfig.EventBusConfig config = new NubeConfig.SystemConfig.EventBusConfig();
        config.getOptions().setClientAuth(ClientAuth.REQUIRED);
        config.getOptions().setSsl(true);
        config.getOptions()
              .setKeyStoreOptions(new JksOptions().setPath("eventBusKeystore.jks").setPassword("nubesparkEventBus"));
        config.getOptions()
              .setTrustStoreOptions(new JksOptions().setPath("eventBusKeystore.jks").setPassword("nubesparkEventBus"));
        System.out.println("DEFAULT: " + config.toJson().encode());
        System.out.println("====================");
        NubeConfig.SystemConfig.EventBusConfig from = IConfig.from(
                "{\"clientAuth\":\"REQUIRED\"," + "\"ssl\":true,\"keyStoreOptions\":{\"path" +
                "\":\"eventBusKeystore.jks\"," + "\"password\":\"nubesparkEventBus\"}," +
                "\"trustStoreOptions\":{\"path" + "\":\"eventBusKeystore.jks\"," +
                "\"password\":\"nubesparkEventBus\"}}", NubeConfig.SystemConfig.EventBusConfig.class);
        System.out.println("FROM: " + from.toJson().encode());
        JSONAssert.assertEquals(from.toJson().encode(), config.toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_merge() {
        JsonObject from = new JsonObject("{\"acceptBacklog\":-1,\"clientAuth\":\"NONE\"}");
        JsonObject to = new JsonObject("{\"acceptBacklog\":-1,\"clientAuth\":\"REQUIRED\"}");
        NubeConfig.SystemConfig.EventBusConfig merge = IConfig.merge(from, to,
                                                                     NubeConfig.SystemConfig.EventBusConfig.class);
        Assert.assertEquals(-1, merge.getOptions().getClusterPublicPort());
        Assert.assertEquals(-1, merge.getOptions().getAcceptBacklog());
        Assert.assertEquals(ClientAuth.REQUIRED, merge.getOptions().getClientAuth());
    }

    @Test
    public void test_merge_from_object() {
        NubeConfig.SystemConfig.EventBusConfig from = IConfig.from("{\"acceptBacklog\":-1,\"clientAuth\":\"NONE\"}",
                                                                   NubeConfig.SystemConfig.EventBusConfig.class);
        NubeConfig.SystemConfig.EventBusConfig to = IConfig.from("{\"acceptBacklog\":-1,\"clientAuth\":\"REQUIRED\"}",
                                                                 NubeConfig.SystemConfig.EventBusConfig.class);
        NubeConfig.SystemConfig.EventBusConfig merge = IConfig.merge(from, to,
                                                                     NubeConfig.SystemConfig.EventBusConfig.class);
        Assert.assertEquals(-1, merge.getOptions().getClusterPublicPort());
        Assert.assertEquals(-1, merge.getOptions().getAcceptBacklog());
        Assert.assertEquals(ClientAuth.REQUIRED, merge.getOptions().getClientAuth());
    }

}
