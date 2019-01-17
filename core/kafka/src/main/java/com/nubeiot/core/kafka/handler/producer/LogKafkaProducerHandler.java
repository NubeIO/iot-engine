package com.nubeiot.core.kafka.handler.producer;

import io.vertx.kafka.client.producer.RecordMetadata;

import com.nubeiot.core.exceptions.ErrorMessage;

/**
 * Responsible for logging metadata after sending Kafka record
 */
public final class LogKafkaProducerHandler extends AbstractKafkaProducerHandler {

    public static final KafkaProducerHandler DEFAULT = new LogKafkaProducerHandler();

    @Override
    public void handleSuccess(RecordMetadata metadata) {
        logger.info("Failed when sending record to Kafka cluster: ", metadata.toJson());
    }

    @Override
    public void handleFailed(ErrorMessage message) {
        logger.error("Failed when sending record to Kafka cluster: {}", message.toJson());
    }

}
