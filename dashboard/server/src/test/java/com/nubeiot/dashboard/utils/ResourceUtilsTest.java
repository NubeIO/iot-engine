package com.nubeiot.dashboard.utils;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.http.RegisterScheme;
import com.nubeiot.core.http.base.HttpScheme;

public class ResourceUtilsTest {

    @Test
    public void test_absPath() {
        new RegisterScheme().register(HttpScheme.HTTPS);
        String absolutePath = ResourceUtils.buildAbsolutePath("localhost:8085", "xyz.jpeg");
        Assert.assertEquals(absolutePath, "https://localhost:8085/xyz.jpeg");
    }

}
