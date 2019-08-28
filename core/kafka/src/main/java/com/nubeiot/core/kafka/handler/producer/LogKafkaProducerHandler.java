package com.nubeiot.core.kafka.handler.producer;

import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.RecordMetadata;

import com.nubeiot.core.exceptions.ErrorMessage;

import lombok.NonNull;

/**
 * Responsible for logging metadata after sending Kafka record
 */
public final class LogKafkaProducerHandler extends AbstractKafkaProducerHandler {

    LogKafkaProducerHandler(@NonNull Vertx vertx, String sharedKey) {
        super(vertx);
        registerSharedKey(sharedKey);
    }

    @Override
    public void handleSuccess(RecordMetadata metadata) {
        logger.info("Sent Kafka record successfully");
        logger.debug("Record metadata: {}", metadata.toJson());
    }

    @Override
    public void handleFailed(ErrorMessage message) {
        logger.error("Sent Kafka record failed: {}", message.toJson());
    }

}
