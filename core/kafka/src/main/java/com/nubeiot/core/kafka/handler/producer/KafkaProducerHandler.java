package com.nubeiot.core.kafka.handler.producer;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.kafka.client.producer.RecordMetadata;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.exceptions.ErrorMessage;

/**
 * Handler after sending record to {@code Kafka cluster}
 */
public interface KafkaProducerHandler extends Handler<AsyncResult<RecordMetadata>>, SharedDataDelegate {

    void handleSuccess(RecordMetadata metadata);

    void handleFailed(ErrorMessage message);

}
