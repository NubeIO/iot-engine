package com.nubeiot.core.kafka;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

import com.nubeiot.core.component.UnitVerticle;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Handle open/close Kafka client
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class KafkaUnit extends UnitVerticle<KafkaConfig> {

    private static final long DEFAULT_CLOSE_TIMEOUT_MS = 30 * 1000;

    private final KafkaRouter router;
    @Getter
    private KafkaConsumerService consumerService;
    @Getter
    private KafkaProducerService producerService;

    @Override
    public Class<KafkaConfig> configClass() { return KafkaConfig.class; }

    @Override
    public String configFile() { return "kafka.json"; }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start();
        this.producerService = KafkaProducerService.create(vertx, this.config.getProducerConfig(), router);
        this.consumerService = KafkaConsumerService.create(vertx, this.config.getConsumerConfig(), router,
                                                           this::getSharedData);
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop();
        List<Completable> completables = new ArrayList<>();
        consumerService.consumers()
                       .parallelStream()
                       .forEach(c -> c.close(event -> close(completables, (AsyncResult) event, "consumer")));
        producerService.producers()
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

}
