package com.nubeiot.core;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.cluster.ClusterType;
import com.nubeiot.core.utils.FileUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class NubeConfig implements IConfig {

    private Path dataDir;
    @JsonProperty(value = SystemConfig.NAME)
    private SystemConfig systemConfig;
    @JsonProperty(value = DeployConfig.NAME)
    private DeployConfig deployConfig = new DeployConfig();
    @JsonProperty(value = AppConfig.NAME)
    private AppConfig appConfig = new AppConfig();

    /**
     * Create {@link NubeConfig} with {@link AppConfig}, default {@link DeployConfig} and without {@link SystemConfig}
     *
     * @param appConfig Given app config
     * @return nubeConfig instance
     */
    public static NubeConfig blank(@NonNull JsonObject appConfig) {
        return blank(FileUtils.DEFAULT_DATADIR, appConfig);
    }

    public static NubeConfig blank(@NonNull Path dataDir, @NonNull JsonObject appConfig) {
        return new NubeConfig(dataDir, null, new DeployConfig(), IConfig.from(appConfig, AppConfig.class));
    }

    public static NubeConfig blank(@NonNull Path dataDir) {
        return new NubeConfig(dataDir, null, new DeployConfig(), null);
    }

    /**
     * Create {@link NubeConfig} with default {@link DeployConfig} and without {@link SystemConfig}
     *
     * @return nubeConfig instance
     */
    public static NubeConfig blank() {
        return NubeConfig.blank(new JsonObject());
    }

    public static NubeConfig constructNubeConfig(NubeConfig nubeConfig, AppConfig appConfig) {
        return IConfig.from(nubeConfig.toJson().mergeIn(new JsonObject().put(AppConfig.NAME, appConfig.toJson())),
                            NubeConfig.class);
    }

    @Override
    public String name() { return null; }

    @Override
    public Class<? extends IConfig> parent() { return null; }

    public Path getDataDir() {
        if (Objects.isNull(dataDir)) {
            dataDir = FileUtils.DEFAULT_DATADIR;
        }
        return dataDir;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class SystemConfig implements IConfig {

        public static final String NAME = "__system__";

        @JsonProperty(value = ClusterConfig.NAME)
        private ClusterConfig clusterConfig = new ClusterConfig();
        @JsonProperty(value = EventBusConfig.NAME)
        private EventBusConfig eventBusConfig = new EventBusConfig();

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
            private String name;
            private ClusterType type = ClusterType.HAZELCAST;
            private String listenerAddress;
            /**
             * URL configuration file
             */
            private String url;
            /**
             * File path configuration file
             */
            private String file;
            private Map<String, Object> options = new HashMap<>();

            @Override
            public String name() { return NAME; }

            @Override
            public Class<? extends IConfig> parent() { return SystemConfig.class; }

        }


        public static final class EventBusConfig extends HashMap<String, Object> implements IConfig {

            public static final String NAME = "__eventBus__";

            @Getter
            @JsonIgnore
            private EventBusOptions options;

            public EventBusConfig() {
                this(null);
            }

            @JsonCreator
            public EventBusConfig(Map<String, Object> map) {
                if (Objects.nonNull(map)) {
                    this.putAll(map);
                }
                this.computeIfPresent("clusterPublicPort", (s, o) -> (int) o == -1 ? null : o);
                this.putIfAbsent("host", "0.0.0.0");
                this.putIfAbsent("port", 5000);
                options = new EventBusOptions(JsonObject.mapFrom(this));
            }

            @Override
            public String name() { return NAME; }

            @Override
            public Class<? extends IConfig> parent() { return SystemConfig.class; }

            @SuppressWarnings("unchecked")
            @Override
            public <T extends IConfig> T merge(@NonNull T to) {
                JsonObject jsonObject = JsonObject.mapFrom(this).mergeIn(new JsonObject(((Map) to)));
                return (T) new EventBusConfig(jsonObject.getMap());
            }

            @Override
            public JsonObject toJson() { return options.toJson(); }

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
