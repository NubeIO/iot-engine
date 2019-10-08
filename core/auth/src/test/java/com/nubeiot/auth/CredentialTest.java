package com.nubeiot.auth;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;

public class CredentialTest {

    @Test
    public void test_basic_credential() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "BASIC");
        jsonObject.put("user", "xx");
        jsonObject.put("password", "abc");
        Credential credential = JsonData.convert(jsonObject, BasicCredential.class);
        Assert.assertEquals(CredentialType.BASIC, credential.getType());
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
        Assert.assertEquals("http://xx:abc@123.com", credential.toUrl("http://123.com"));
    }

    @Test
    public void test_basic_credential_get_header() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "BASIC");
        jsonObject.put("user", "xx");
        jsonObject.put("password", "abc");
        Credential credential = JsonData.convert(jsonObject, BasicCredential.class);
        Assert.assertEquals("Basic eHg6YWJj", credential.toHeader());
    }

    @Test
    public void test_token_credential() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "TOKEN");
        jsonObject.put("token", "abcdef");
        Credential credential = JsonData.convert(jsonObject, TokenCredential.class);
        Assert.assertEquals(CredentialType.TOKEN, credential.getType());
        Assert.assertEquals("abcdef", ((TokenCredential) credential).getToken());
    }

    @Test
    public void test_token_credential_get_url() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "TOKEN");
        jsonObject.put("token", "abcdef");
        Credential credential = JsonData.convert(jsonObject, TokenCredential.class);
        Assert.assertEquals("http://abcdef@123.com", credential.toUrl("http://123.com"));
    }

    @Test
    public void test_token_credential_get_header() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "TOKEN");
        jsonObject.put("token", "abcdef");
        Credential credential = JsonData.convert(jsonObject, TokenCredential.class);
        Assert.assertEquals("Bearer abcdef", credential.toHeader());
    }

    @Test
    public void test_compute_url() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "BASIC");
        jsonObject.put("user", "xx");
        jsonObject.put("password", "abc");
        BasicCredential credential = JsonData.convert(jsonObject, BasicCredential.class);
        Assert.assertEquals("https://xx:abc@abc.xyz", credential.toUrl("https://abc.xyz"));
        Assert.assertEquals("http://xx:abc@abc.xyz", credential.toUrl("http://abc.xyz"));
        Assert.assertEquals("ws://xx:abc@abc.xyz", credential.toUrl("ws://abc.xyz"));
        Assert.assertEquals("wss://xx:abc@abc.xyz", credential.toUrl("wss://abc.xyz"));
    }

}
