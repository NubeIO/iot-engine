package com.nubeiot.edge.installer.model.type;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.NonNull;

/**
 * Represents {@code Executable Binary} module type.
 *
 * @since 1.0.0
 */
public interface ExecutableBinaryModuleType extends ModuleType {

    ExecutableBinaryModuleType NODEJS_BINARY = new ExecutableBinaryModuleType() {
        @Override
        public String type() {
            return "NODEJS_BINARY";
        }

        @Override
        public String generateFQN(String appId, String version, String serviceName) {
            return null;
        }
    };

    @JsonCreator
    static ExecutableBinaryModuleType factory(@NonNull String type) {
        return ModuleTypeFactory.factory(type, ExecutableBinaryModuleType.class);
    }

    @Override
    default String protocol() {
        return "binary";
    }

}
