package com.nubeiot.edge.installer.loader;

import java.util.Objects;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.utils.Strings;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

//TODO Later for other languages (except JAVA)
public interface ModuleType {

    static ModuleType getDefault() {
        return JAVA;
    }

    @JsonCreator
    static ModuleType factory(String type) {
        if (JAVASCRIPT.name().equalsIgnoreCase(type)) {
            return JAVASCRIPT;
        }
        if (RUBY.name().equalsIgnoreCase(type)) {
            return RUBY;
        }
        if (GROOVY.name().equalsIgnoreCase(type)) {
            return GROOVY;
        }
        if (SCALA.name().equalsIgnoreCase(type)) {
            return SCALA;
        }
        if (KOTLIN.name().equalsIgnoreCase(type)) {
            return KOTLIN;
        }
        return getDefault();
    }

    String name();

    ModuleType JAVA = new AbstractModuleType() {

        @Override
        public String name() {
            return "JAVA";
        }

        @Override
        public String generateFQN(String serviceId, String version, String serviceName) {
            return String.format("maven:%s:%s::%s", serviceId, Strings.isBlank(version) ? DEFAULT_VERSION : version,
                                 serviceName);
        }

        private static final String DEFAULT_GROUP_ID = "com.nubeiot.edge.connector";
        private static final String DEFAULT_VERSION = "1.0.0";

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
                        .put("service_id", serviceId)
                        .put("service_name", serviceName)
                        .put("service_type", name());
        }

    };
    ModuleType JAVASCRIPT = new AbstractModuleType() {
        @Override
        public String name() {
            return "JAVASCRIPT";
        }

        @Override
        public String generateFQN(String serviceId, String version, String serviceName) {
            return null;
        }

        @Override
        public JsonObject serialize(JsonObject input, ModuleTypeRule rule) throws InvalidModuleType {
            return null;
        }

    };
    ModuleType GROOVY = new AbstractModuleType() {
        @Override
        public String name() {
            return "GROOVY";
        }

        @Override
        public String generateFQN(String serviceId, String version, String serviceName) {
            return null;
        }

        @Override
        public JsonObject serialize(JsonObject input, ModuleTypeRule rule) throws InvalidModuleType {
            return null;
        }

    };


    @EqualsAndHashCode
    abstract class AbstractModuleType implements ModuleType {

        @EqualsAndHashCode.Include
        public abstract String name();

        @Override
        public final String toString() {
            return this.name();
        }

    }


    ModuleType SCALA = new AbstractModuleType() {
        @Override
        public String name() {
            return "SCALA";
        }

        @Override
        public String generateFQN(String serviceId, String version, String serviceName) {
            return null;
        }

        @Override
        public JsonObject serialize(JsonObject input, ModuleTypeRule rule) throws InvalidModuleType {
            return null;
        }

    };
    ModuleType KOTLIN = new AbstractModuleType() {
        @Override
        public String name() {
            return "KOTLIN";
        }

        @Override
        public String generateFQN(String serviceId, String version, String serviceName) {
            return null;
        }

        @Override
        public JsonObject serialize(JsonObject input, ModuleTypeRule rule) throws InvalidModuleType {
            return null;
        }

    };
    ModuleType RUBY = new AbstractModuleType() {
        @Override
        public String name() {
            return "RUBY";
        }

        @Override
        public String generateFQN(String serviceId, String version, String serviceName) {
            return null;
        }

        @Override
        public JsonObject serialize(JsonObject input, ModuleTypeRule rule) throws InvalidModuleType {
            return null;
        }

    };

    String generateFQN(String serviceId, String version, String serviceName);

    JsonObject serialize(@NonNull JsonObject input, @NonNull ModuleTypeRule rule) throws InvalidModuleType;

}
