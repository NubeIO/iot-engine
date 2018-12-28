package com.nubeiot.edge.connector.bonescript.jwt;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.TestBase;

public class JwtTokenAuthenticationTest extends TestBase {
    @Test
    public void testInvalidToken() {
        String accessToken = "Invalid Token";
        JwtTokenAuthentication<JwtUserPrincipal, JwtUserPrincipal> jwtTokenAuthentication = new JwtTokenAuthentication<>();
        JwtUserPrincipal jwtUserPrincipal = new JwtUserPrincipal(accessToken);
        jwtUserPrincipal = jwtTokenAuthentication.apply(jwtUserPrincipal);
        Assert.assertFalse(jwtUserPrincipal.getAuthorized());
    }

    @Test
    public void testValidToken() {
        String accessToken = JwtAccessTokenProvider.create();
        JwtTokenAuthentication<JwtUserPrincipal, JwtUserPrincipal> jwtTokenAuthentication = new JwtTokenAuthentication<>();
        JwtUserPrincipal jwtUserPrincipal = new JwtUserPrincipal(accessToken);
        jwtUserPrincipal = jwtTokenAuthentication.apply(jwtUserPrincipal);
        Assert.assertTrue(jwtUserPrincipal.getAuthorized());
    }
}
