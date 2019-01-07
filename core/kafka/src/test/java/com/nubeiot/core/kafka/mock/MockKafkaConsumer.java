package com.nubeiot.core.kafka.mock;

import java.util.Collections;
import java.util.function.Supplier;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaReadStream;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.kafka.supplier.KafkaReaderSupplier;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockKafkaConsumer {

    private final Vertx vertx;
    private final JsonObject consumerCfg;
    private final String topic;
    private final Supplier<EventModel> eventModelSupplier;
    private KafkaReadStream<String, EventMessage> consumer;

    public void start() throws Exception {
        EventController controller = new EventController(vertx);
        consumer = KafkaReaderSupplier.create(vertx, consumerCfg, String.class, EventMessage.class);
        consumer.handler(record -> {
            System.err.println("CONSUMER Topic: " + record.topic());
            System.err.println(record.value().toJson().encodePrettily());
            EventModel eventModel = eventModelSupplier.get();
            controller.response(eventModel.getAddress(), eventModel.getPattern(), record.value());
        }).exceptionHandler(Throwable::printStackTrace);
        consumer.subscribe(Collections.singleton(topic));
    }

    public void stop() throws Exception {
        if (consumer != null) {
            consumer.close();
        }
    }

}
