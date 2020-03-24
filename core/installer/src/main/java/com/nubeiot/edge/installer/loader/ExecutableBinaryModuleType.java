package com.nubeiot.edge.installer.loader;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.edge.installer.loader.AbstractModuleType.AbstractExecutableBinaryModuleType;

import lombok.NonNull;

/**
 * Represents {@code Executable Binary} module type.
 *
 * @since 1.0.0
 */
public interface ExecutableBinaryModuleType extends ModuleType {

    ExecutableBinaryModuleType JAVASCRIPT_BINARY = new AbstractExecutableBinaryModuleType() {
        @Override
        public String name() {
            return "JAVASCRIPT_BINARY";
        }

        @Override
        public String generateFQN(String appId, String version, String serviceName) {
            return null;
        }

        @Override
        public JsonObject serialize(JsonObject input, ModuleTypeRule rule) throws InvalidModuleType {
            return null;
        }
    };

    @JsonCreator
    static ExecutableBinaryModuleType factory(@NonNull String type) {
        return AbstractModuleType.factory(type, ExecutableBinaryModuleType.class);
    }

}
