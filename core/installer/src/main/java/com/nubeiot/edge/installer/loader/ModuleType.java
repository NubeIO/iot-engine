package com.nubeiot.edge.installer.loader;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;
import com.nubeiot.edge.installer.model.tables.pojos.Application;

import lombok.NonNull;

/**
 * The interface Module type.
 *
 * @since 1.0.0
 */
public interface ModuleType extends EnumType {

    /**
     * Defines default module type
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
        return ModuleTypeFactory.factory(type);
    }

    /**
     * Defines module type
     *
     * @return the module type
     * @since 1.0.0
     */
    String type();

    /**
     * Defines service factory protocol to get artifact from remote/local repository
     *
     * @return the service factory protocol
     * @since 1.0.0
     */
    String protocol();

    /**
     * Generate full qualified name.
     *
     * @param appId       the app id
     * @param version     the version
     * @param serviceName the service name
     * @return the string
     * @since 1.0.0
     */
    String generateFQN(@NonNull String appId, String version, String serviceName);

    /**
     * Serialize request json to application model.
     *
     * @param request the input
     * @return the application
     * @throws InvalidModuleType the invalid module type
     * @see IApplication
     * @since 1.0.0
     */
    default IApplication serialize(@NonNull JsonObject request) throws InvalidModuleType {
        return new Application(request).setServiceType(this);
    }

}
