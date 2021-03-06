package com.nubeiot.core.kafka.mock;

import java.util.Collections;
import java.util.function.Supplier;

import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.kafka.KafkaConfig.ConsumerCfg;
import com.nubeiot.core.kafka.supplier.KafkaConsumerProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockKafkaConsumer {

    private final Vertx vertx;
    private final ConsumerCfg consumerCfg;
    private final String topic;
    private final Supplier<EventModel> eventModelSupplier;
    private KafkaConsumer<String, EventMessage> consumer;

    public void start() {
        EventbusClient controller = SharedDataDelegate.getEventController(vertx, this.getClass().getName());
        consumer = KafkaConsumerProvider.create(vertx, consumerCfg, String.class, EventMessage.class);
        consumer.handler(record -> {
            System.err.println("CONSUMER Topic: " + record.topic());
            System.err.println(record.value().toJson().encodePrettily());
            EventModel eventModel = eventModelSupplier.get();
            controller.fire(eventModel.getAddress(), eventModel.getPattern(), record.value());
        }).exceptionHandler(Throwable::printStackTrace);
        consumer.subscribe(Collections.singleton(topic));
    }

    public void stop() {
        if (consumer != null) {
            consumer.close();
        }
    }

}
