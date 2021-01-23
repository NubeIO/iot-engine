package com.nubeiot.core.kafka;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;

import com.nubeiot.core.component.UnitContext;
import com.nubeiot.core.kafka.service.KafkaConsumerService;
import com.nubeiot.core.kafka.service.KafkaProducerService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class KafkaContext extends UnitContext {

    private static final long DEFAULT_CLOSE_TIMEOUT_MS = 30 * 1000L;
    private KafkaConsumerService consumerService;
    private KafkaProducerService producerService;

    void create(Vertx vertx, KafkaConfig config, KafkaRouter router, String sharedKey) {
        this.producerService = KafkaProducerService.create(vertx, config.getProducerConfig(), router, sharedKey);
        this.consumerService = KafkaConsumerService.create(vertx, config.getConsumerConfig(), router, sharedKey);
    }

    Completable stop() {
        List<Completable> completables = new ArrayList<>();
        consumerService.consumers()
                       .parallelStream()
                       .forEach(
                           c -> c.close(event -> close(completables, (AsyncResult) event, KafkaClientType.CONSUMER)));
        producerService.producers()
                       .parallelStream()
                       .forEach(p -> p.close(DEFAULT_CLOSE_TIMEOUT_MS, event -> close(completables, (AsyncResult) event,
                                                                                      KafkaClientType.PRODUCER)));
        return Completable.merge(completables);
    }

    private void close(List<Completable> completables, AsyncResult event, KafkaClientType type) {
        if (event.failed()) {
            logger.error("Failed when close Kafka {}", event.cause(), type);
        }
        completables.add(Completable.complete());
    }

}
