package com.nubeiot.core.component;

import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.mock.MockConfig;

import lombok.NonNull;

public class HasConfigTest {

    @Test
    public void test_not_found_config_file() {
        MockHasConfig hasConfig = new MockHasConfig();
        final MockConfig config = hasConfig.computeConfig(new JsonObject());
        System.out.println(config.toJson());
    }

    @Test(expected = NubeException.class)
    public void test_invalid_config() {
        MockHasConfig hasConfig = new MockHasConfig();
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
