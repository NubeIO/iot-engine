package com.nubeiot.core.component;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;

import lombok.NoArgsConstructor;
import lombok.NonNull;

public class HasConfigTest {

    static class MockHasConfig implements HasConfig<MockIConfig> {

        @Override
        public @NonNull Class<MockIConfig> configClass() {
            return MockIConfig.class;
        }

        @Override
        public @NonNull String configFile() {
            return "notfound.json";
        }

    }


    @NoArgsConstructor
    static class MockIConfig implements IConfig {

        @Override
        public String name() {
            return "xxx";
        }

        @Override
        public Class<? extends IConfig> parent() {
            return null;
        }

    }

    @Test
    public void test_not_found_config_file() {
        MockHasConfig hasConfig = new MockHasConfig();
        final MockIConfig config = hasConfig.computeConfig(new JsonObject());
        System.out.println(config.toJson());
    }

    @Test(expected = NubeException.class)
    public void test_invalid_config() {
        MockHasConfig hasConfig = new MockHasConfig();
        final MockIConfig config = hasConfig.computeConfig(new JsonObject().put("aaa","yyy"));
    }
}
