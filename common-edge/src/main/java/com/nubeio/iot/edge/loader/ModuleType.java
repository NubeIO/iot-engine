package com.nubeio.iot.edge.loader;

import com.nubeio.iot.share.utils.Strings;

import io.vertx.core.json.JsonObject;
import lombok.EqualsAndHashCode;

public interface ModuleType {

    String name();

    JsonObject serialize(JsonObject input) throws InvalidModuleType;

    JsonObject deserialize(String serviceId) throws InvalidModuleType;

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

        static final String DEFAULT_GROUP_ID = "com.nubeio.iot.edge";
        static final String DEFAULT_VERSION = "1.0.0";

        @Override
        public JsonObject serialize(JsonObject input) throws InvalidModuleType {
            final String artifactId = input.getString("artifact_id");
            final String serviceName = input.getString("service_name", artifactId);
            if (Strings.isBlank(artifactId)) {
                throw new InvalidModuleType("Missing artifact id");
            }
            String serviceId = String.format("maven:%s:%s:%s::%s", input.getString("group_id", DEFAULT_GROUP_ID),
                                             artifactId, input.getString("version", DEFAULT_VERSION), serviceName);
            return input.mergeIn(new JsonObject(), true).put("service_id", serviceId).put("service_type", name());
        }

        @Override
        public JsonObject deserialize(String serviceId) throws InvalidModuleType {
            return null;
        }
    };

    ModuleType JAVASCRIPT = new AbstractModuleType() {
        @Override
        public String name() {
            return "JAVASCRIPT";
        }

        @Override
        public JsonObject serialize(JsonObject input) throws InvalidModuleType {
            return null;
        }

        @Override
        public JsonObject deserialize(String serviceId) throws InvalidModuleType {
            return null;
        }
    };

}
