package com.nubeiot.edge.connector.sample.kafka;

import java.util.UUID;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.kafka.KafkaEventMetadata;
import com.nubeiot.core.kafka.KafkaRouter;
import com.nubeiot.core.kafka.KafkaUnit;
import com.nubeiot.core.kafka.KafkaUnitProvider;
import com.nubeiot.core.kafka.service.KafkaProducerService;

public final class EdgeKafkaDemo extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        this.addProvider(new KafkaUnitProvider(initKafkaRouter()), this::startProducer);
    }

    private KafkaRouter initKafkaRouter() {
        return new KafkaRouter().registerKafkaEvent(
            KafkaEventMetadata.producer().topic("GPIO").keyClass(String.class).valueClass(String.class).build());
    }

    private void startProducer(KafkaUnit kafkaUnit) {
        logger.info("Starting Producer Service...");
        KafkaProducerService producerService = kafkaUnit.getProducerService();
        vertx.setPeriodic(3000, id -> {
            logger.info("Sending data...");
            producerService.publish(EventAction.CREATE, "GPIO", 1, this.deploymentID(), UUID.randomUUID().toString());
        });
    }

}
