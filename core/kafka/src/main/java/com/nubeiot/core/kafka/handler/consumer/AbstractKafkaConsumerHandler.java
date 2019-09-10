package com.nubeiot.core.kafka.handler.consumer;

import java.util.Objects;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;

/**
 * @see KafkaConsumerHandler
 */
public abstract class AbstractKafkaConsumerHandler<K, V, T extends KafkaConsumerRecordTransformer<K, V, R>, R>
    extends AbstractSharedDataDelegate<KafkaConsumerHandler> implements KafkaConsumerHandler<K, V, T, R> {

    private T transformer;

    @Override
    public final KafkaConsumerHandler registerTransformer(T transformer) {
        if (Objects.nonNull(transformer)) {
            this.transformer = transformer;
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final T transformer() {
        return Objects.isNull(this.transformer) ? (T) KafkaConsumerRecordTransformer.DEFAULT : this.transformer;
    }

}
