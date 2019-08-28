package com.nubeiot.core.kafka.handler.producer;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.RecordMetadata;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.kafka.service.KafkaProducerService;

import lombok.NonNull;

/**
 * Kafka Producer Handler is represented for
 * <ul>
 * <li>{@code Producer Record} transformer before sending record to {@code Kafka cluster}</li>
 * <li>{@code Completion handler} after sending record to {@code Kafka cluster}</li>
 * </ul>
 *
 * @see KafkaProducerRecordTransformer
 * @see SharedDataDelegate
 * @see RecordMetadata
 */
public interface KafkaProducerHandler<T extends KafkaProducerRecordTransformer>
    extends Handler<AsyncResult<RecordMetadata>> {

    static KafkaProducerHandler logHandler(@NonNull Vertx vertx, String sharedKey) {
        return new LogKafkaProducerHandler(vertx, sharedKey);
    }

    void handleSuccess(RecordMetadata metadata);

    void handleFailed(ErrorMessage message);

    /**
     * Translates value and includes {@code EventAction} before sending to {@code Kafka cluster}
     *
     * @return transformer
     * @see KafkaProducerService#publish(EventAction, String, Object)
     * @see EventAction
     */
    T transformer();

    /**
     * System will register it automatically. You don't need call it directly
     *
     * @param transformer Given transformer
     * @param sharedKey   Shared key to access local data
     * @return a reference to this, so the API can be used fluently
     */
    KafkaProducerHandler register(T transformer, String sharedKey);

}
