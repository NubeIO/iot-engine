package com.nubeiot.edge.installer.loader;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.edge.installer.loader.AbstractModuleType.AbstractExecutableArchiverVertxModuleType;

import lombok.NonNull;

public interface ExecutableArchiverModuleType extends ExecutableBinaryModuleType {

    ExecutableArchiverModuleType JAVASCRIPT_ARCHIVER = new AbstractExecutableArchiverVertxModuleType() {
        @Override
        public ExecutableBinaryModuleType binaryType() {
            return ExecutableBinaryModuleType.JAVASCRIPT_BINARY;
        }

        @Override
        public String name() {
            return "JAVASCRIPT_BINARY_ARCHIVER";
        }

        @Override
        public String generateFQN(String appId, String version, String serviceName) {
            return binaryType().generateFQN(appId, version, serviceName);
        }

        @Override
        public JsonObject serialize(JsonObject input, ModuleTypeRule rule) throws InvalidModuleType {
            return binaryType().serialize(input, rule);
        }
    };

    @JsonCreator
    static ExecutableArchiverModuleType factory(@NonNull String type) {
        return AbstractModuleType.factory(type, ExecutableArchiverModuleType.class);
    }

    @NonNull ExecutableBinaryModuleType binaryType();

}
