package com.nubeiot.core.kafka;

import io.vertx.core.Future;

import com.nubeiot.core.component.UnitVerticle;

/**
 * Handle open/close Kafka client
 */
public final class KafkaUnit extends UnitVerticle<KafkaConfig, KafkaContext> {

    private final KafkaRouter router;

    KafkaUnit(KafkaRouter router) {
        super(new KafkaContext());
        this.router = router;
    }

    @Override
    public Class<KafkaConfig> configClass() { return KafkaConfig.class; }

    @Override
    public String configFile() { return "kafka.json"; }

    @Override
    public void start() {
        logger.info("Starting Kafka Unit...");
        super.start();
        this.getContext().create(vertx, config, router, this::getSharedData);
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop();
        this.getContext().stop().subscribe(stopFuture::complete);
    }

}
