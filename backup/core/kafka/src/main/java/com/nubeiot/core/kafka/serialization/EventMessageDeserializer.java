package com.nubeiot.core.kafka.serialization;

import java.util.Map;
import java.util.Objects;

import org.apache.kafka.common.serialization.Deserializer;

import io.vertx.core.buffer.Buffer;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventMessage;

public final class EventMessageDeserializer implements Deserializer<EventMessage> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) { }

    @Override
    public EventMessage deserialize(String topic, byte[] data) {
        return Objects.isNull(data) ? null : EventMessage.tryParse(JsonData.tryParse(Buffer.buffer(data)));
    }

    @Override
    public void close() { }

}
