package com.nubeiot.core.kafka.handler.producer;

import io.vertx.kafka.client.producer.RecordMetadata;

import com.nubeiot.core.exceptions.ErrorMessage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Responsible for logging metadata after sending Kafka record
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class LogKafkaProducerHandler extends AbstractKafkaProducerHandler {

    @Override
    public void handleSuccess(RecordMetadata metadata) {
        logger.info("Failed when sending record to Kafka cluster: ", metadata.toJson());
    }

    @Override
    public void handleFailed(ErrorMessage message) {
        logger.error("Failed when sending record to Kafka cluster: {}", message.toJson());
    }

}
