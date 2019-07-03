package com.nubeiot.auth;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.auth.Credential.CredentialType;
import com.nubeiot.core.NubeConfig.SecretConfig;
import com.nubeiot.core.dto.JsonData;

public class CredentialTest {

    @Test
    public void test_basic_credential() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "BASIC");
        jsonObject.put("user", "xx");
        jsonObject.put("password", "abc");
        Credential credential = JsonData.convert(jsonObject, BasicCredential.class);
        Assert.assertEquals(Credential.CredentialType.BASIC, credential.getType());
        Assert.assertEquals("xx", credential.getUser());
        Assert.assertEquals("abc", ((BasicCredential) credential).getPassword());
    }

    @Test
    public void test_basic_credential_get_url() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "BASIC");
        jsonObject.put("user", "xx");
        jsonObject.put("password", "abc");
        Credential credential = JsonData.convert(jsonObject, BasicCredential.class);
        Assert.assertEquals("http://xx:abc@123.com", credential.computeUrl("http://123.com", new SecretConfig()));
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
        Assert.assertEquals("http://abcdef@123.com", credential.computeUrl("http://123.com", new SecretConfig()));
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
        Assert.assertEquals("https://xx:abc@abc.xyz", credential.computeUrl("https://abc.xyz", new SecretConfig()));
        Assert.assertEquals("http://xx:abc@abc.xyz", credential.computeUrl("http://abc.xyz", new SecretConfig()));
        Assert.assertEquals("ws://xx:abc@abc.xyz", credential.computeUrl("ws://abc.xyz", new SecretConfig()));
        Assert.assertEquals("wss://xx:abc@abc.xyz", credential.computeUrl("wss://abc.xyz", new SecretConfig()));
    }

}
