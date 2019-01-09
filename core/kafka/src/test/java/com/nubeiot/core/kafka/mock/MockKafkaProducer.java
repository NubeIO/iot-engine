package com.nubeiot.core.kafka.mock;

import java.util.function.Supplier;

import org.apache.kafka.clients.producer.ProducerRecord;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaWriteStream;

import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.kafka.supplier.KafkaWriterSupplier;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockKafkaProducer {

    private final Vertx vertx;
    private final JsonObject producerCfg;
    private final String topic;
    private final Supplier<EventMessage> messageSupplier;
    private KafkaWriteStream<String, EventMessage> producer;

    public void start() {
        producer = KafkaWriterSupplier.create(vertx, producerCfg, String.class, EventMessage.class);
        vertx.setPeriodic(2000, id -> {
            EventMessage message = messageSupplier.get();
            System.err.println("PRODUCER ID: " + id);
            System.err.println(message.toJson().encodePrettily());
            producer.unwrap().send(new ProducerRecord<>(topic, message));
        });
    }

    public void stop() {
        if (producer != null) {
            producer.close();
        }
    }

}
