package com.nubeiot.edge.connector.bonescript.jwt;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.TestBase;
import com.nubeiot.core.utils.Strings;

public class JwtAccessTokenProviderTest extends TestBase {
    @Test
    public void testGetAccessToken() {
        Assert.assertTrue(Strings.isNotBlank(JwtAccessTokenProvider.create()));
    }
}
