package com.nubeiot.edge.installer.loader;

import java.util.Objects;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.utils.Reflections.ReflectionMethod;

import lombok.NonNull;

/**
 * The interface Module type.
 *
 * @since 1.0.0
 */
//TODO Later for other languages (except JAVA) https://github.com/NubeIO/iot-engine/issues/239
public interface ModuleType {

    /**
     * Gets default.
     *
     * @return the default
     * @since 1.0.0
     */
    static ModuleType getDefault() {
        return VertxModuleType.JAVA;
    }

    /**
     * Factory module type.
     *
     * @param type the type
     * @return the module type
     * @since 1.0.0
     */
    @JsonCreator
    static ModuleType factory(String type) {
        return Stream.of(VertxModuleType.class, ExecutableBinaryModuleType.class, ExecutableArchiverModuleType.class)
                     .map(clazz -> ReflectionMethod.executeStatic(clazz, "factory", type))
                     .filter(Objects::nonNull)
                     .map(ModuleType.class::cast)
                     .findFirst()
                     .orElseGet(ModuleType::getDefault);
    }

    /**
     * Name string.
     *
     * @return the string
     * @since 1.0.0
     */
    String name();

    /**
     * Generate fqn string.
     *
     * @param appId       the app id
     * @param version     the version
     * @param serviceName the service name
     * @return the string
     * @since 1.0.0
     */
    String generateFQN(@NonNull String appId, String version, String serviceName);

    /**
     * Serialize json object.
     *
     * @param input the input
     * @param rule  the rule
     * @return the json object
     * @throws InvalidModuleType the invalid module type
     * @since 1.0.0
     */
    JsonObject serialize(@NonNull JsonObject input, @NonNull ModuleTypeRule rule) throws InvalidModuleType;

}
