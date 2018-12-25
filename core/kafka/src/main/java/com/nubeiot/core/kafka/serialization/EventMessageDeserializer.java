package com.nubeiot.core.kafka.serialization;

import java.util.Map;
import java.util.Objects;

import org.apache.kafka.common.serialization.Deserializer;

import com.nubeiot.core.event.EventMessage;

import io.vertx.core.buffer.Buffer;

public class EventMessageDeserializer implements Deserializer<EventMessage> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public EventMessage deserialize(String topic, byte[] data) {
        return Objects.isNull(data) ? null : EventMessage.from(Buffer.buffer(data).toJsonObject());
    }

    @Override
    public void close() {
    }

}
