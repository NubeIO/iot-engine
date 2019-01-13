package com.nubeiot.core.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;

import io.reactivex.Completable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.kafka.client.consumer.KafkaReadStream;
import io.vertx.kafka.client.producer.KafkaWriteStream;

import com.nubeiot.core.component.UnitVerticle;
import com.nubeiot.core.exceptions.StateException;
import com.nubeiot.core.kafka.KafkaConfig.ConsumerCfg;
import com.nubeiot.core.kafka.KafkaConfig.ProducerCfg;
import com.nubeiot.core.kafka.handler.ConsumerDispatcher;
import com.nubeiot.core.kafka.supplier.KafkaReaderSupplier;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Handle open/close Kafka client
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class KafkaUnit extends UnitVerticle<KafkaConfig> {

    private static final long DEFAULT_CLOSE_TIMEOUT_MS = 30 * 1000;

    private final KafkaRouter router;
    private Map<String, KafkaReadStream> consumers = new HashMap<>();
    private Map<String, KafkaWriteStream> producers = new HashMap<>();

    @Override
    public Class<KafkaConfig> configClass() { return KafkaConfig.class; }

    @Override
    public String configFile() { return "kafka.json"; }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start();
        router.getKafkaEvents();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop();
        List<Completable> completables = new ArrayList<>();
        consumers.values()
                 .parallelStream()
                 .forEach(c -> c.close(event -> close(completables, (AsyncResult) event, "consumer")));
        producers.values()
                 .parallelStream()
                 .forEach(p -> p.close(DEFAULT_CLOSE_TIMEOUT_MS,
                                       event -> close(completables, (AsyncResult) event, "producer")));
        Completable.merge(completables).subscribe(stopFuture::complete);
    }

    private void close(List<Completable> completables, AsyncResult event, String type) {
        if (event.failed()) {
            logger.error("Failed serialize close {}", event.cause(), type);
        }
        completables.add(Completable.complete());
    }

    /**
     * Create Kafka consumer
     *
     * @param <K>        Type of key
     * @param <V>        Type of value
     * @param keyClass   Class of key data
     * @param valueClass Class of value data
     * @param dispatcher Consumer dispatcher
     * @return Kafka consumer client
     * @throws StateException if {@code type of key} is already existed
     * @see Consumer
     */
    public <K, V> Consumer<K, V> createConsumer(@NonNull Class<K> keyClass, @NonNull Class<V> valueClass,
                                                @NonNull ConsumerDispatcher<K, V> dispatcher) {
        String key = validate(consumers, keyClass, valueClass);
        ConsumerCfg consumerConfig = this.config.getConsumerConfig();
        KafkaReadStream<K, V> consumer = KafkaReaderSupplier.create(vertx, consumerConfig, keyClass, valueClass);
        consumer.handler(dispatcher::accept).exceptionHandler(t -> logger.error("Failed when on the read stream", t));
        consumers.put(key, consumer);
        return consumer.subscribe(dispatcher.topics()).unwrap();
    }

    /**
     * Create Kafka producer client
     *
     * @param keyClass Class of key data
     * @param <K>      Type of key
     * @return Kafka producer client
     * @throws StateException if {@code type of key} is already existed
     * @see Producer
     */
    public <K, V> Producer<K, V> createProducer(Class<K> keyClass, Class<V> valueClass) {
        String key = validate(producers, keyClass, valueClass);
        ProducerCfg producerConfig = this.config.getProducerConfig();
        KafkaWriteStream<K, V> producer = KafkaWriteStream.create(vertx, producerConfig, keyClass, valueClass);
        producer.exceptionHandler(t -> logger.error("Failed when on the write stream", t));
        producers.put(key, producer);
        return producer.unwrap();
    }

    private <K, V> String validate(Map<String, ?> checker, Class<K> keyClass, Class<V> valueClass) {
        String key = keyClass.getName() + "::" + valueClass.getName();
        if (checker.containsKey(key)) {
            throw new StateException(Strings.format("Key {0} is already existed", key));
        }
        return key;
    }

}
