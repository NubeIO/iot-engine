package com.nubeiot.core;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.FileUtils;

public class IConfigTest {

    private static final URL RESOURCE = IConfigTest.class.getClassLoader().getResource("nube-cfg.json");

    @Test
    public void test_recompute_references() {
        JsonObject input = new JsonObject(FileUtils.readFileToString(RESOURCE.toString()));
        NubeConfig nubeConfig = IConfig.from(input, NubeConfig.class);
        JsonObject output = IConfig.recomputeReferences(nubeConfig, nubeConfig.getSecretConfig()).toJson();

        Assert.assertFalse(output.encode().contains("@secret"));
    }

}
