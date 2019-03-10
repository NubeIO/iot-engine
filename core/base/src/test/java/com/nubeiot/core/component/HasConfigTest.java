package com.nubeiot.core.component;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.mock.MockConfig;

import lombok.NonNull;

public class HasConfigTest {

    private MockHasConfig hasConfig;

    @Before
    public void SetUp() {
        hasConfig = new MockHasConfig();
    }

    @Test
    public void test_not_found_config_file_should_get_default_value() {
        final MockConfig config = hasConfig.computeConfig(new JsonObject());
        Assert.assertNotNull(config);
    }

    @Test(expected = NubeException.class)
    public void test_invalid_config_should_throw_exception() {
        hasConfig.computeConfig(new JsonObject().put("aaa", "yyy"));
    }

    static class MockHasConfig implements HasConfig<MockConfig> {

        @Override
        public @NonNull Class<MockConfig> configClass() {
            return MockConfig.class;
        }

        @Override
        public @NonNull String configFile() {
            return "notfound.json";
        }

    }

}
