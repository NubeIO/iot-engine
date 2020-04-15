package com.nubeiot.core;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.zero.utils.FileUtils;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.cluster.ClusterType;

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

    public static final String DATA_DIR = "dataDir";
    public static final Path DEFAULT_DATADIR = FileUtils.defaultDatadir(".nubeio");

    @JsonProperty(value = NubeConfig.DATA_DIR)
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
        return blank(DEFAULT_DATADIR, appConfig);
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

    public static NubeConfig create(NubeConfig nubeConfig, AppConfig appConfig) {
        return IConfig.from(nubeConfig.toJson().mergeIn(new JsonObject().put(AppConfig.NAME, appConfig.toJson())),
                            NubeConfig.class);
    }

    @Override
    public String key() { return null; }

    @Override
    public Class<? extends IConfig> parent() { return null; }

    public Path getDataDir() {
        if (Objects.isNull(dataDir)) {
            dataDir = DEFAULT_DATADIR;
        }
        return dataDir;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class SystemConfig implements IConfig {

        public static final String NAME = "__system__";

        @JsonProperty(value = ClusterConfig.KEY_NAME)
        private ClusterConfig clusterConfig = new ClusterConfig();
        @JsonProperty(value = EventBusConfig.NAME)
        private EventBusConfig eventBusConfig = new EventBusConfig();

        @Override
        public String key() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return NubeConfig.class; }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static final class ClusterConfig implements IConfig {

            public static final String KEY_NAME = "__cluster__";

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
            public String key() { return KEY_NAME; }

            @Override
            public Class<? extends IConfig> parent() { return SystemConfig.class; }

        }


        public static final class EventBusConfig extends HashMap<String, Object> implements IConfig {

            public static final String NAME = "__eventBus__";

            public static final String DELIVERY_OPTIONS = "__delivery__";

            @Getter
            @JsonIgnore
            private EventBusOptions options;

            @Getter
            @JsonIgnore
            private DeliveryOptions deliveryOptions;

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
                this.options = new EventBusOptions(JsonObject.mapFrom(this));
                this.deliveryOptions = createDeliveryConfig();
            }

            private DeliveryOptions createDeliveryConfig() {
                Object deliveryConfig = this.get(DELIVERY_OPTIONS);
                if (Objects.isNull(deliveryConfig)) {
                    return new DeliveryOptions();
                }
                return new DeliveryOptions(JsonObject.mapFrom(deliveryConfig));
            }

            @Override
            public String key() { return NAME; }

            @Override
            public Class<? extends IConfig> parent() { return SystemConfig.class; }

            @SuppressWarnings("unchecked")
            @Override
            public <T extends IConfig> T merge(@NonNull T to) {
                JsonObject jsonObject = JsonObject.mapFrom(this).mergeIn(new JsonObject(((Map) to)));
                return (T) new EventBusConfig(jsonObject.getMap());
            }

            @Override
            public JsonObject toJson() {
                return options.toJson().put(DELIVERY_OPTIONS, this.deliveryOptions.toJson());
            }

        }

    }


    public static final class DeployConfig extends DeploymentOptions implements IConfig {

        public static final String NAME = "__deploy__";

        @Override
        public String key() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return NubeConfig.class; }

    }


    public static final class AppConfig extends HashMap<String, Object> implements IConfig {

        public static final String NAME = "__app__";

        @Override
        public String key() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return NubeConfig.class; }

    }

}
