package com.nubeiot.auth;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.auth.Credential.CredentialType;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig.AppSecretConfig;
import com.nubeiot.core.SecretProperty;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Configs;

public class CredentialTest {

    @Test
    public void test_basic_credential() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "BASIC");
        jsonObject.put("user", "xx");
        jsonObject.put("password", "abc");
        BasicCredential credential = JsonData.convert(jsonObject, BasicCredential.class);
        Assert.assertEquals(Credential.CredentialType.BASIC, credential.getType());
        Assert.assertEquals("xx", credential.getUser());
        Assert.assertEquals("abc", credential.getPassword());
    }

    @Test
    public void test_basic_credential_get_url() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "BASIC");
        jsonObject.put("user", "xx");
        jsonObject.put("password", "abc");
        Credential credential = JsonData.convert(jsonObject, BasicCredential.class);
        Assert.assertEquals("http://xx:abc@123.com", credential.computeUrl("http://123.com"));
    }

    @Test
    public void test_basic_credential_get_header() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "BASIC");
        jsonObject.put("user", "xx");
        jsonObject.put("password", "abc");
        Credential credential = JsonData.convert(jsonObject, BasicCredential.class);
        Assert.assertEquals("Basic eHg6YWJj", credential.computeHeader());
    }

    @Test
    public void test_token_credential() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "TOKEN");
        jsonObject.put("token", "abcdef");
        TokenCredential credential = JsonData.convert(jsonObject, TokenCredential.class);
        Assert.assertEquals(CredentialType.TOKEN, credential.getType());
        Assert.assertEquals("abcdef", credential.getToken());
    }

    @Test
    public void test_token_credential_get_url() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "TOKEN");
        jsonObject.put("token", "abcdef");
        Credential credential = JsonData.convert(jsonObject, TokenCredential.class);
        Assert.assertEquals("http://abcdef@123.com", credential.computeUrl("http://123.com"));
    }

    @Test
    public void test_token_credential_get_header() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "TOKEN");
        jsonObject.put("token", "abcdef");
        Credential credential = JsonData.convert(jsonObject, TokenCredential.class);
        Assert.assertEquals("Bearer abcdef", credential.computeHeader());
    }

    @Test
    public void test_compute_url() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "BASIC");
        jsonObject.put("user", "xx");
        jsonObject.put("password", "abc");
        BasicCredential credential = JsonData.convert(jsonObject, BasicCredential.class);
        Assert.assertEquals("https://xx:abc@abc.xyz", credential.computeUrl("https://abc.xyz"));
        Assert.assertEquals("http://xx:abc@abc.xyz", credential.computeUrl("http://abc.xyz"));
        Assert.assertEquals("ws://xx:abc@abc.xyz", credential.computeUrl("ws://abc.xyz"));
        Assert.assertEquals("wss://xx:abc@abc.xyz", credential.computeUrl("wss://abc.xyz"));
    }

    @Test
    public void test_basic_secret_credential() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "BASIC_SECRET");
        jsonObject.put("user", new SecretProperty("@secret.user", "xx").toJson());
        jsonObject.put("password", new SecretProperty("@secret.password", "abc").toJson());
        BasicSecretCredential credential = JsonData.convert(jsonObject, BasicSecretCredential.class);
        Assert.assertEquals(CredentialType.BASIC_SECRET, credential.getType());
        Assert.assertEquals("xx", credential.getUser().getValue());
        Assert.assertEquals("abc", credential.getPassword().getValue());
        Assert.assertEquals("@secret.user", credential.getUser().getRef());
        Assert.assertEquals("@secret.password", credential.getPassword().getRef());
    }

    @Test
    public void test_recompute_reference_credential() {
        NubeConfig nubeConfig = IConfig.from(Configs.loadJsonConfig("nube-cfg.json"), NubeConfig.class);
        AppSecretConfig secretConfig = IConfig.from(nubeConfig.getAppConfig(), AppSecretConfig.class);
        JsonObject output = Credential.recomputeReferenceCredentials(nubeConfig, secretConfig).toJson();
        Assert.assertTrue(output.encode().contains("BASIC_SECRET"));
    }

}
