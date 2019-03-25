package com.nubeiot.core.http;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.http.base.HttpScheme;

public class RegisterSchemeTest {

    @Test
    public void test_schema() {
        new RegisterScheme().register(HttpScheme.HTTPS);

        Assert.assertEquals(RegisterScheme.getInstance().getHttpScheme(), HttpScheme.HTTPS);

        new RegisterScheme().register(HttpScheme.HTTP);

        Assert.assertEquals(RegisterScheme.getInstance().getHttpScheme(), HttpScheme.HTTPS);
    }

}
