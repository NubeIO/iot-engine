package com.nubeiot.core.micro;

import java.util.Objects;
import java.util.function.Supplier;

import io.reactivex.Single;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.micro.MicroConfig.CircuitBreakerConfig;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class CircuitBreakerController implements Supplier<CircuitBreaker> {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerController.class);

    private final CircuitBreaker circuitBreaker;

    static CircuitBreakerController create(Vertx vertx, CircuitBreakerConfig config) {
        if (config.isEnabled()) {
            logger.info("Circuit Breaker Config : {}", config.toJson().encode());
            return new CircuitBreakerController(
                CircuitBreaker.create(config.getCircuitName(), vertx, config.getOptions()));
        }
        logger.info("Skip setup circuit breaker");
        return new CircuitBreakerController(null);
    }

    @Override
    public CircuitBreaker get() {
        return Objects.requireNonNull(this.circuitBreaker, "Circuit breaker is not enabled");
    }

    public <T> Single<T> wrap(Single<T> command) {
        if (Objects.isNull(circuitBreaker)) {
            return command;
        }
        return circuitBreaker.rxExecuteCommand(event -> command.subscribe(event::complete, event::fail));
    }

}
