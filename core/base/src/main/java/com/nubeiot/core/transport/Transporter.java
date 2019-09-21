package com.nubeiot.core.transport;

import io.vertx.core.Vertx;

/**
 * Transporter can be {@code HTTP client}, {@code Eventbus client}, {@code Kafka client}, {@code MQTT client}
 */
public interface Transporter {

    Vertx getVertx();

}
