package com.nubeiot.edge.connector.bonescript.jwt;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.TestBase;
import com.nubeiot.core.dto.JsonData;

import io.vertx.core.json.JsonObject;

public class JwtUserPrincipalTest extends TestBase {
    @Test
    public void testDecodingTest() throws JSONException {
        JwtUserPrincipal jwtUserPrincipal = new JwtUserPrincipal("token");
        jwtUserPrincipal.setAuthorized(true);
        jwtUserPrincipal.setRole(Role.User);
        JSONAssert.assertEquals(jwtUserPrincipal.toJson().encode(), new JsonObject().put("accessToken", "token").put("authorized", true).put("role", Role.User).encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void testEncodingTest() throws JSONException {
        JsonObject data = new JsonObject().put("accessToken", "token").put("authorized", true).put("role", Role.User)
                                          .put("username", "username").put("password", "password");
        JwtUserPrincipal jwtUserPrincipal = JsonData.from(data, JwtUserPrincipal.class);
        Assert.assertEquals(jwtUserPrincipal.getAccessToken(), "token");
        Assert.assertEquals(jwtUserPrincipal.getAuthorized(), true);
        Assert.assertEquals(jwtUserPrincipal.getUsername(), "username");
        Assert.assertEquals(jwtUserPrincipal.getPassword(), "password");
        JSONAssert.assertEquals(jwtUserPrincipal.toJson().encode(), "{\"accessToken\":\"token\",\"authorized\":true," +
                                                      "\"role\":\"User\",\"username\":\"username\"}", JSONCompareMode.STRICT);
        Assert.assertEquals(jwtUserPrincipal.getRole(), Role.User);
    }
}
