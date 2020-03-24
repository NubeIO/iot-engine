package com.nubeiot.edge.installer.loader;

import java.util.Objects;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.utils.Reflections.ReflectionMethod;

import lombok.NonNull;

//TODO Later for other languages (except JAVA) https://github.com/NubeIO/iot-engine/issues/239
public interface ModuleType {

    static ModuleType getDefault() {
        return VertxModuleType.JAVA;
    }

    @JsonCreator
    static ModuleType factory(String type) {
        return Stream.of(VertxModuleType.class, ExecutableBinaryModuleType.class, ExecutableArchiverModuleType.class)
                     .map(clazz -> ReflectionMethod.executeStatic(clazz, "factory", type))
                     .filter(Objects::nonNull)
                     .map(ModuleType.class::cast)
                     .findFirst()
                     .orElseGet(ModuleType::getDefault);
    }

    String name();

    String generateFQN(@NonNull String appId, String version, String serviceName);

    JsonObject serialize(@NonNull JsonObject input, @NonNull ModuleTypeRule rule) throws InvalidModuleType;

}
