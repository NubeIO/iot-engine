package com.nubeiot.edge.core.loader;

import java.util.Objects;

import com.nubeiot.core.utils.Strings;

import io.vertx.core.json.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

//TODO Later for other languages (except JAVA)
public interface ModuleType {

    String name();

    JsonObject serialize(JsonObject input, ModuleTypeRule rule) throws InvalidModuleType;

    @EqualsAndHashCode
    abstract class AbstractModuleType implements ModuleType {

        @EqualsAndHashCode.Include
        public abstract String name();

        @Override
        public final String toString() {
            return this.name();
        }

    }


    ModuleType JAVA = new AbstractModuleType() {
        @Override
        public String name() {
            return "JAVA";
        }

        static final String DEFAULT_GROUP_ID = "com.nubeiot.edge";
        static final String DEFAULT_VERSION = "1.0.0";

        @Override
        public JsonObject serialize(@NonNull JsonObject input, ModuleTypeRule rule) throws InvalidModuleType {
            final String artifactId = input.getString("artifact_id");
            final String groupId = input.getString("group_id", DEFAULT_GROUP_ID);
            final String serviceName = input.getString("service_name", artifactId);
            if (Strings.isBlank(artifactId)) {
                throw new InvalidModuleType("Missing artifact_id");
            }
            if (Objects.nonNull(rule) && !rule.getRule(this).test(groupId + "." + artifactId)) {
                throw new InvalidModuleType("Artifact is not valid");
            }
            String serviceId = String.format("maven:%s:%s:%s::%s", groupId, artifactId,
                                             input.getString("version", DEFAULT_VERSION), serviceName);
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
        public JsonObject serialize(JsonObject input, ModuleTypeRule rule) throws InvalidModuleType {
            return null;
        }
    };

    ModuleType SCALA = new AbstractModuleType() {
        @Override
        public String name() {
            return "SCALA";
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
        public JsonObject serialize(JsonObject input, ModuleTypeRule rule) throws InvalidModuleType {
            return null;
        }
    };

}
