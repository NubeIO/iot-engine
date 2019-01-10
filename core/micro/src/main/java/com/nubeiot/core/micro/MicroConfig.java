package com.nubeiot.core.micro;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;

import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class MicroConfig implements IConfig {

    public static final String NAME = "__micro__";

    @JsonProperty(value = ServiceDiscoveryConfig.NAME)
    private ServiceDiscoveryConfig discoveryConfig = new ServiceDiscoveryConfig();
    @JsonProperty(value = CircuitBreakerConfig.NAME)
    private CircuitBreakerConfig circuitConfig = new CircuitBreakerConfig();

    @Override
    public String name() { return NAME; }

    @Override
    public Class<? extends IConfig> parent() { return NubeConfig.AppConfig.class; }

    public static class ServiceDiscoveryConfig extends ServiceDiscoveryOptions implements IConfig {

        public static final String NAME = "__serviceDiscovery__";

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return MicroConfig.class; }

    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CircuitBreakerConfig implements IConfig {

        public static final String NAME = "__circuitBreaker__";

        @JsonProperty(value = "name")
        private String circuitName = "nubeio-circuit-breaker";
        private CircuitBreakerOptions options = new CircuitBreakerOptions();

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return MicroConfig.class; }

    }

}
