package com.nubeiot.core.kafka.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Singular;

@Builder(builderClassName = "Builder")
public class ConsumerDispatcher<K, V> implements Consumer<ConsumerRecord<K, V>> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NonNull
    @Singular("consumer")
    private final Map<String, ConsumerHandler> consumersByTopic = new HashMap<>();
    @NonNull
    @Default
    private final ConsumerRecordTransformer<K, V> transformer = new ConsumerRecordTransformer<>();

    public Set<String> topics() {
        return consumersByTopic.keySet();
    }

    @Override
    public void accept(ConsumerRecord<K, V> record) {
        ConsumerHandler consumerHandler = consumersByTopic.get(record.topic());
        if (Objects.isNull(consumerHandler)) {
            logger.warn("No handler for topic {}", record.topic());
        }
        consumerHandler.accept(transformer.apply(record));
    }

}
