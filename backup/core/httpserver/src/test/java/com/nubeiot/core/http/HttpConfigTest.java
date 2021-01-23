package com.nubeiot.core.http;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.utils.Configs;

public class HttpConfigTest {

    @Test
    public void test_default() throws JSONException {
        HttpConfig config = new HttpConfig();
        System.out.println(config.toJson().encode());
        HttpConfig fromFile = IConfig.from(Configs.loadJsonConfig("httpServer.json"), HttpConfig.class);
        JSONAssert.assertEquals(fromFile.toJson().encode(), config.toJson().encode(), JSONCompareMode.LENIENT);
    }

    @Test
    public void deserialize() {
        String jsonStr = "{\"__app__\":{\"__http__\":{\"publicHost\":\"1.1.1.1\",\"publicScheme\":\"HTTPS\"," +
                         "\"host\":\"1.1.1.1\",\"port\":9090,\"__rest__\":{\"enabled\":true,\"rootApi\":\"/xyz\"}}}}";
        HttpConfig from = IConfig.from(jsonStr, HttpConfig.class);
        Assert.assertNotNull(from);
        Assert.assertEquals("1.1.1.1", from.getHost());
        Assert.assertEquals(9090, from.getPort());
        Assert.assertEquals("/xyz", from.getRestConfig().getRootApi());
        Assert.assertEquals("https://1.1.1.1", from.publicServerUrl());
        Assert.assertTrue(from.getRestConfig().isEnabled());
        Assert.assertNotNull(from.getOptions());
        Assert.assertTrue(from.getOptions().isCompressionSupported());
        Assert.assertTrue(from.getOptions().isDecompressionSupported());
        Assert.assertFalse(from.getWebsocketConfig().isEnabled());
        Assert.assertFalse(from.getHttp2Cfg().isEnabled());
    }

}
