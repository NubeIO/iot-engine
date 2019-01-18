package com.nubeiot.core.kafka.mock;

import java.util.function.Consumer;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.kafka.client.producer.RecordMetadata;

import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.kafka.handler.producer.AbstractKafkaProducerHandler;

import lombok.Builder;
import lombok.NonNull;

@Builder(builderClassName = "Builder")
public class TestProducerHandler extends AbstractKafkaProducerHandler {

    @NonNull
    private final TestContext context;
    @NonNull
    private final Async async;
    @NonNull
    private final Consumer<Async> countdown;
    private final String topic;
    private final int partition;
    private final ErrorMessage errorMessage;

    @Override
    public void handleSuccess(RecordMetadata metadata) {
        try {
            context.assertNotNull(metadata);
            context.assertEquals(topic, metadata.getTopic());
            context.assertEquals(partition, metadata.getPartition());
        } finally {
            countdown.accept(async);
        }
    }

    @Override
    public void handleFailed(ErrorMessage message) {
        try {
            context.assertNotNull(message);
            context.assertEquals(errorMessage, message);
        } finally {
            countdown.accept(async);
        }
    }

}
