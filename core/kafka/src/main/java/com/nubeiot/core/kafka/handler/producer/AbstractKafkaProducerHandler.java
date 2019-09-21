package com.nubeiot.core.kafka.handler.producer;

import java.util.Objects;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.producer.RecordMetadata;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;
import com.nubeiot.core.exceptions.ErrorMessage;

import lombok.NonNull;

/**
 * @see KafkaProducerHandler
 */
public abstract class AbstractKafkaProducerHandler<T extends KafkaProducerRecordTransformer>
    extends AbstractSharedDataDelegate<KafkaProducerHandler> implements KafkaProducerHandler<T> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private T transformer;

    protected AbstractKafkaProducerHandler(@NonNull Vertx vertx) {
        super(vertx);
    }

    @Override
    public final void handle(AsyncResult<RecordMetadata> result) {
        if (result.succeeded()) {
            handleSuccess(result.result());
        } else {
            handleFailed(ErrorMessage.parse(result.cause()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T transformer() {
        return Objects.isNull(this.transformer) ? (T) KafkaProducerRecordTransformer.DEFAULT : this.transformer;
    }

    @Override
    public final KafkaProducerHandler register(T transformer, String sharedKey) {
        if (Objects.nonNull(transformer)) {
            this.transformer = transformer;
        }
        return registerSharedKey(sharedKey);
    }

}
