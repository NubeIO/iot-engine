package com.nubeiot.core;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.cluster.ClusterType;
import com.nubeiot.core.utils.FileUtils;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.KeyCertOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class NubeConfig implements IConfig {

    private Path dataDir = FileUtils.DEFAULT_DATADIR;
    @JsonProperty(value = SystemConfig.NAME)
    private SystemConfig systemConfig = new SystemConfig();
    @JsonProperty(value = DeployConfig.NAME)
    private DeployConfig deployConfig = new DeployConfig();
    @JsonProperty(value = AppConfig.NAME)
    private AppConfig appConfig = new AppConfig();

    @Override
    public String name() { return null; }

    @Override
    public Class<? extends IConfig> parent() { return null; }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class SystemConfig implements IConfig {

        public static final String NAME = "__system__";

        @JsonProperty(value = ClusterConfig.NAME)
        private ClusterConfig clusterConfig = new ClusterConfig();
        @JsonProperty(value = EventBusConfig.NAME)
        private EventBusConfig eventBusConfig = new EventBusConfig();
        @JsonProperty(value = MicroConfig.NAME)
        private MicroConfig microConfig = new MicroConfig();

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return NubeConfig.class; }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static final class ClusterConfig implements IConfig {

            public static final String NAME = "__cluster__";

            private boolean active = false;
            private boolean ha = false;
            private String name = "";
            private ClusterType type = ClusterType.HAZELCAST;
            private String listenerAddress = "";
            /**
             * URL configuration file
             */
            private String url = "";
            /**
             * File path configuration file
             */
            private String file = "";
            private Map<String, Object> options = new HashMap<>();

            @Override
            public String name() { return NAME; }

            @Override
            public Class<? extends IConfig> parent() { return SystemConfig.class; }

        }


        public static final class EventBusConfig extends EventBusOptions implements IConfig {

            public static final String NAME = "__eventBus__";

            public EventBusConfig() {
                this.setClusterPublicPort(this.getPort());
            }

            @JsonCreator
            private EventBusConfig(Map<String, Object> options) {
                super(fixClusterPort(options));
            }

            private static JsonObject fixClusterPort(Map<String, Object> options) {
                if (options == null) {
                    options = new HashMap<>();
                }
                options.merge("clusterPublicPort", options.getOrDefault("port", 0),
                              (o, o2) -> (Integer) o < 0 ? o2 : o);
                return JsonObject.mapFrom(options);
            }

            @Override
            public String name() { return NAME; }

            @Override
            public Class<? extends IConfig> parent() { return SystemConfig.class; }

            @Override
            public JsonObject toJson() { return super.toJson(); }

            @JsonIgnore
            @Override
            public EventBusOptions setKeyCertOptions(KeyCertOptions options) {
                return super.setKeyCertOptions(options);
            }

        }


        public static final class MicroConfig extends HashMap implements IConfig {

            public static final String NAME = "__micro__";

            @Override
            public String name() { return NAME; }

            @Override
            public Class<? extends IConfig> parent() { return SystemConfig.class; }

        }

    }


    public static final class DeployConfig extends DeploymentOptions implements IConfig {

        public static final String NAME = "__deploy__";

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return NubeConfig.class; }

    }


    public static final class AppConfig extends HashMap implements IConfig {

        public static final String NAME = "__app__";

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return NubeConfig.class; }

    }

}
