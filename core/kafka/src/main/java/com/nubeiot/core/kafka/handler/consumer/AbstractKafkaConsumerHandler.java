package com.nubeiot.core.kafka.handler.consumer;

import java.util.Objects;

import io.vertx.core.Vertx;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;

import lombok.NonNull;

/**
 * @see KafkaConsumerHandler
 */
public abstract class AbstractKafkaConsumerHandler<K, V, T extends KafkaConsumerRecordTransformer<K, V, R>, R>
    extends AbstractSharedDataDelegate<KafkaConsumerHandler> implements KafkaConsumerHandler<K, V, T, R> {

    private T transformer;

    protected AbstractKafkaConsumerHandler(@NonNull Vertx vertx) {
        super(vertx);
    }

    @Override
    public final KafkaConsumerHandler register(T transformer, String sharedKey) {
        if (Objects.nonNull(transformer)) {
            this.transformer = transformer;
        }
        return registerSharedKey(sharedKey);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final T transformer() {
        return Objects.isNull(this.transformer) ? (T) KafkaConsumerRecordTransformer.DEFAULT : this.transformer;
    }

}
