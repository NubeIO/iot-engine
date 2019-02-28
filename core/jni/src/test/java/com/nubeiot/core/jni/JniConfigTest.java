package com.nubeiot.core.jni;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.utils.Configs;

public class JniConfigTest {

    @Test
    public void test_default() {
        JniConfig jniConfig = IConfig.from(Configs.loadJsonConfig("jniConfig.json"), JniConfig.class);
        System.out.println(jniConfig.toJson());
        Assert.assertEquals(jniConfig.getLibDir(), "/home/");
        Assert.assertEquals(jniConfig.getLib(), "example");
    }

}
