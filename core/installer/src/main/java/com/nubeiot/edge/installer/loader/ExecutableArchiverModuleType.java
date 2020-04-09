package com.nubeiot.edge.installer.loader;

import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;

import lombok.NonNull;

public interface ExecutableArchiverModuleType extends ExecutableBinaryModuleType {

    @NonNull ExecutableBinaryModuleType binaryType();

    @Override
    default String generateFQN(String appId, String version, String serviceName) {
        return protocol() + binaryType().generateFQN(appId, version, serviceName);
    }

    @Override
    default IApplication serialize(@NonNull JsonObject request) throws InvalidModuleType {
        return binaryType().serialize(request);
    }

}
