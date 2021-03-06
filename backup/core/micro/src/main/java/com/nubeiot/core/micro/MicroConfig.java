package com.nubeiot.core.micro;

import java.util.HashMap;
import java.util.Map;

import io.github.zero88.utils.Strings;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.impl.DefaultServiceDiscoveryBackend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.micro.monitor.ServiceGatewayAnnounceMonitor;
import com.nubeiot.core.micro.monitor.ServiceGatewayUsageMonitor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class MicroConfig implements IConfig {

    public static final String NAME = "__micro__";

    private String serviceName;
    @JsonProperty(value = GatewayConfig.NAME)
    private GatewayConfig gatewayConfig = new GatewayConfig();
    @JsonProperty(value = ServiceDiscoveryConfig.NAME)
    private ServiceDiscoveryConfig discoveryConfig = new ServiceDiscoveryConfig();
    @JsonProperty(value = LocalServiceDiscoveryConfig.NAME)
    private LocalServiceDiscoveryConfig localDiscoveryConfig = new LocalServiceDiscoveryConfig();
    @JsonProperty(value = CircuitBreakerConfig.NAME)
    private CircuitBreakerConfig circuitConfig = new CircuitBreakerConfig();

    @Override
    public String key() { return NAME; }

    @Override
    public Class<? extends IConfig> parent() { return AppConfig.class; }

    @Getter
    public static class ServiceDiscoveryConfig extends ServiceDiscoveryOptions implements IConfig {

        public static final String NAME = "__serviceDiscovery__";

        public static final String NUBE_ANNOUNCE_ADDRESS = "nubeio.service.discovery.announce";
        public static final String NUBE_USAGE_ADDRESS = "nubeio.service.discovery.usage";

        private boolean enabled;
        private BackendConfig backendConfig;

        ServiceDiscoveryConfig() { this(true, new BackendConfig(false)); }

        ServiceDiscoveryConfig(boolean enabled, BackendConfig backendConfig) {
            this.enabled = enabled;
            this.backendConfig = backendConfig;
            this.reload();
        }

        @Override
        public String key() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return MicroConfig.class; }

        @JsonIgnore
        boolean isLocal() { return false; }

        @JsonIgnore
        String getNubeAnnounceAddress() { return NUBE_ANNOUNCE_ADDRESS; }

        @JsonIgnore
        String getNubeUsageAddress() { return NUBE_USAGE_ADDRESS; }

        private void reload() {
            reloadProperty();
            String announceAddress = this.getAnnounceAddress();
            String usageAddress = this.getUsageAddress();
            this.setAnnounceAddress(Strings.isBlank(announceAddress) || DEFAULT_ANNOUNCE_ADDRESS.equals(announceAddress)
                                    ? getNubeAnnounceAddress()
                                    : announceAddress);
            this.setUsageAddress(Strings.isBlank(usageAddress)
                                 ? null
                                 : DEFAULT_USAGE_ADDRESS.equals(usageAddress) ? getNubeUsageAddress() : usageAddress);
            this.setAutoRegistrationOfImporters(!isLocal()).setBackendConfiguration(backendConfig.toConfiguration());
        }

        void reloadProperty() {
            System.setProperty(BackendConfig.DEFAULT_SERVICE_DISCOVERY_BACKEND, String.valueOf(isLocal()));
        }

        @Override
        public JsonObject toJson() {
            return super.toJson().put("enabled", enabled);
        }

    }


    public static class LocalServiceDiscoveryConfig extends ServiceDiscoveryConfig {

        public static final String NAME = "__localServiceDiscovery__";

        public static final String NUBE_ANNOUNCE_LOCAL_ADDRESS = "nubeio.service.local.discovery.announce";
        public static final String DEFAULT_USAGE_LOCAL_ADDRESS = "nubeio.service.local.discovery.usage";

        LocalServiceDiscoveryConfig() { super(false, new BackendConfig(true)); }

        @Override
        public String key() { return NAME; }

        @JsonIgnore
        boolean isLocal() { return true; }

        @JsonIgnore
        String getNubeAnnounceAddress() { return NUBE_ANNOUNCE_LOCAL_ADDRESS; }

        @JsonIgnore
        String getNubeUsageAddress() { return DEFAULT_USAGE_LOCAL_ADDRESS; }

    }


    @Getter
    static class BackendConfig implements JsonData {

        static final String DEFAULT_SERVICE_DISCOVERY_BACKEND = "vertx-service-discovery-backend-local";
        static final String BACKEND_NAME = "backend-name";
        @JsonUnwrapped
        private final Map<String, Object> map = new HashMap<>();
        @JsonIgnore
        private boolean local;
        private String className;

        BackendConfig(boolean local) {
            this.local = local;
            this.className = DefaultServiceDiscoveryBackend.class.getName();
        }

        @JsonCreator
        BackendConfig(@JsonProperty(value = "className") String className) {
            this.className = Strings.isBlank(className) ? DefaultServiceDiscoveryBackend.class.getName() : className;
        }

        /**
         * To {@code Vertx configuration}
         *
         * @return json represents for {@code Vertx configuration}
         */
        JsonObject toConfiguration() {
            return new JsonObject(map).put(BackendConfig.BACKEND_NAME, className).put("local", isLocal());
        }

    }


    @Getter
    public static class CircuitBreakerConfig implements IConfig {

        public static final String NAME = "__circuitBreaker__";
        public static final String DEFAULT_NOTIFICATION_ADDRESS = "nubeio.circuit.breaker";

        @JsonProperty(value = "name")
        private String circuitName = "nubeio-circuit-breaker";
        private boolean enabled = false;
        private CircuitBreakerOptions options = new CircuitBreakerOptions().setNotificationAddress(
            DEFAULT_NOTIFICATION_ADDRESS);

        @Override
        public String key() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return MicroConfig.class; }

    }


    @Getter
    public static class GatewayConfig implements IConfig {

        static final String NAME = "__gateway__";

        private boolean enabled = false;
        private String indexAddress = "nubeio.service.gateway.index";
        private String clusterAnnounceMonitorClass = ServiceGatewayAnnounceMonitor.class.getName();
        private String clusterUsageMonitorClass = ServiceGatewayUsageMonitor.class.getName();
        private String localAnnounceMonitorClass = ServiceGatewayAnnounceMonitor.class.getName();
        private String localUsageMonitorClass = ServiceGatewayUsageMonitor.class.getName();

        @Override
        public String key() {
            return NAME;
        }

        @Override
        public Class<? extends IConfig> parent() {
            return MicroConfig.class;
        }

    }

}
