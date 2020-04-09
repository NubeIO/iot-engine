package com.nubeiot.edge.installer.model.type;

import java.util.Arrays;
import java.util.function.Predicate;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.installer.model.InstallerApiIndex.ApplicationMetadata;
import com.nubeiot.edge.installer.model.InvalidModuleType;
import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;
import com.nubeiot.edge.installer.model.tables.pojos.Application;

import lombok.NonNull;

/**
 * Represents {@code Vertx} polyglot module type.
 *
 * @since 1.0.0
 */
public interface VertxModuleType extends ModuleType {

    /**
     * The constant DEFAULT_VERSION.
     */
    String DEFAULT_VERSION = "1.0.0";

    /**
     * The constant JAVA.
     */
    VertxModuleType JAVA = (JVMModuleType) () -> "JAVA";

    /**
     * The constant GROOVY.
     */
    VertxModuleType GROOVY = (JVMModuleType) () -> "GROOVY";

    /**
     * The constant KOTLIN.
     */
    VertxModuleType KOTLIN = (JVMModuleType) () -> "KOTLIN";

    /**
     * The constant SCALA.
     */
    VertxModuleType SCALA = (JVMModuleType) () -> "SCALA";

    /**
     * The constant JAVASCRIPT.
     */
    VertxModuleType JAVASCRIPT = new VertxModuleType() {
        @Override
        public String type() {
            return "JAVASCRIPT";
        }

        @Override
        public String protocol() {
            return null;
        }

        @Override
        public String generateFQN(String appId, String version, String serviceName) {
            return null;
        }
    };

    /**
     * The constant RUBY.
     */
    VertxModuleType RUBY = new VertxModuleType() {
        @Override
        public String type() {
            return "RUBY";
        }

        @Override
        public String protocol() {
            return null;
        }

        @Override
        public String generateFQN(String appId, String version, String serviceName) {
            return null;
        }
    };

    /**
     * Factory vertx module type.
     *
     * @param type the type
     * @return the vertx module type
     * @since 1.0.0
     */
    @JsonCreator
    static VertxModuleType factory(@NonNull String type) {
        return ModuleTypeFactory.factory(type, VertxModuleType.class);
    }

    /**
     * The interface {@code JVM} module type.
     *
     * @since 1.0.0
     */
    interface JVMModuleType extends VertxModuleType {

        String DEFAULT_GROUP_ID = "com.nubeiot.edge.connector";

        static Predicate<String> rulePredicate(@NonNull String... artifactGroups) {
            return appId -> {
                if (Strings.isBlank(appId)) {
                    return false;
                }
                final String group = appId.replaceAll(":", ".");
                return Arrays.stream(artifactGroups).parallel().anyMatch(group::startsWith);
            };
        }

        @Override
        default String protocol() {
            return "maven";
        }

        @Override
        default String generateFQN(String appId, String version, String serviceName) {
            return String.format("%s:%s:%s::%s", protocol(), appId,
                                 Strings.isBlank(version) ? DEFAULT_VERSION : version, serviceName);
        }

        @Override
        default IApplication serialize(@NonNull JsonObject request) throws InvalidModuleType {
            final com.nubeiot.edge.installer.model.tables.@NonNull Application table
                = ApplicationMetadata.INSTANCE.table();
            final String idField = table.getJsonField(table.APP_ID);
            final String serviceId = request.getString(idField);
            if (Strings.isNotBlank(serviceId)) {
                return VertxModuleType.super.serialize(request);
            }
            final String artifactId = request.getString("artifact_id");
            final String groupId = request.getString("group_id", DEFAULT_GROUP_ID);
            final String serviceName = request.getString("service_name", artifactId);
            if (Strings.isBlank(artifactId)) {
                throw new InvalidModuleType("Missing artifact_id");
            }
            return new Application(request.mergeIn(new JsonObject(), true)
                                          .put(idField, String.format("%s:%s", groupId, artifactId))
                                          .put(table.getJsonField(table.SERVICE_NAME), serviceName)
                                          .put(table.getJsonField(table.SERVICE_TYPE), type())).setServiceType(this);
        }

    }

}
