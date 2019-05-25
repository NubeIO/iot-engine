package com.nubeiot.auth;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.auth.Credential.CredentialType;
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
        Assert.assertEquals("xx", ((BasicCredential) credential).getUser());
        Assert.assertEquals("abc", ((BasicCredential) credential).getPassword());
    }

    @Test
    public void test_token_credential() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("type", "TOKEN");
        jsonObject.put("token", "abcdeffafsdfs");
        Credential credential = JsonData.convert(jsonObject, TokenCredential.class);
        Assert.assertEquals(CredentialType.TOKEN, credential.getType());
        Assert.assertEquals("abcdeffafsdfs", ((TokenCredential) credential).getToken());
    }

}
