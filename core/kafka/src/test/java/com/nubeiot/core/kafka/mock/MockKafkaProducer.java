package com.nubeiot.core.kafka.mock;

import java.util.function.Supplier;

import org.apache.kafka.clients.producer.ProducerRecord;

import com.nubeiot.core.component.IComponent;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.kafka.KafkaWriterSupplier;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaWriteStream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockKafkaProducer implements IComponent {

    private final Vertx vertx;
    private final JsonObject producerCfg;
    private final String topic;
    private final Supplier<EventMessage> messageSupplier;
    private KafkaWriteStream<String, EventMessage> producer;

    @Override
    public void start() throws NubeException {
        producer = KafkaWriterSupplier.create(vertx, producerCfg, String.class, EventMessage.class);
        vertx.setPeriodic(2000, id -> {
            EventMessage message = messageSupplier.get();
            System.err.println("PRODUCER ID: " + id);
            System.err.println(message.toJson().encodePrettily());
            producer.write(new ProducerRecord<>(topic, message));
        });
    }

    @Override
    public void stop() throws NubeException {
        if (producer != null) {
            producer.close();
        }
    }

}
