package com.nubeiot.edge.installer.loader;

import java.util.Objects;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.installer.loader.AbstractModuleType.AbstractVertxModuleType;

import lombok.NonNull;

/**
 * Represents {@code Vertx}  polyglot module type.
 *
 * @since 1.0.0
 */
public interface VertxModuleType extends ModuleType {

    /**
     * The constant JAVA.
     */
    VertxModuleType JAVA = new AbstractVertxModuleType() {

        private static final String DEFAULT_GROUP_ID = "com.nubeiot.edge.connector";
        private static final String DEFAULT_VERSION = "1.0.0";

        @Override
        public String name() {
            return "JAVA";
        }

        @Override
        public String generateFQN(String appId, String version, String serviceName) {
            return String.format("maven:%s:%s::%s", appId, Strings.isBlank(version) ? DEFAULT_VERSION : version,
                                 serviceName);
        }

        @Override
        public JsonObject serialize(JsonObject input, ModuleTypeRule rule) throws InvalidModuleType {
            final String artifactId = input.getString("artifact_id");
            final String groupId = input.getString("group_id", DEFAULT_GROUP_ID);
            final String serviceName = input.getString("service_name", artifactId);
            if (Strings.isBlank(artifactId)) {
                throw new InvalidModuleType("Missing artifact_id");
            }
            if (Objects.nonNull(rule) && !rule.getRule(this).test(groupId + "." + artifactId)) {
                throw new InvalidModuleType("Artifact is not valid");
            }
            String serviceId = String.format("%s:%s", groupId, artifactId);
            return input.mergeIn(new JsonObject(), true)
                        .put("app_id", serviceId)
                        .put("service_name", serviceName)
                        .put("service_type", name());
        }
    };
    /**
     * The constant JAVASCRIPT.
     */
    VertxModuleType JAVASCRIPT = new AbstractVertxModuleType() {
        @Override
        public String name() {
            return "JAVASCRIPT";
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
    /**
     * The constant GROOVY.
     */
    VertxModuleType GROOVY = new AbstractVertxModuleType() {
        @Override
        public String name() {
            return "GROOVY";
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
    /**
     * The constant SCALA.
     */
    VertxModuleType SCALA = new AbstractVertxModuleType() {
        @Override
        public String name() {
            return "SCALA";
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
    /**
     * The constant KOTLIN.
     */
    VertxModuleType KOTLIN = new AbstractVertxModuleType() {
        @Override
        public String name() {
            return "KOTLIN";
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
    /**
     * The constant RUBY.
     */
    VertxModuleType RUBY = new AbstractVertxModuleType() {
        @Override
        public String name() {
            return "RUBY";
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

    /**
     * Factory vertx module type.
     *
     * @param type the type
     * @return the vertx module type
     * @since 1.0.0
     */
    @JsonCreator
    static VertxModuleType factory(@NonNull String type) {
        return AbstractModuleType.factory(type, VertxModuleType.class);
    }

}
