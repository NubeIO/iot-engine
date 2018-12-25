package com.nubeiot.core.kafka.serialization;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import com.nubeiot.core.event.EventMessage;

import io.vertx.kafka.client.serialization.VertxSerdes;

public class NubeKafkaSerdes extends Serdes {

    public static final class EventMessageSerde extends WrapperSerde<EventMessage> {

        public EventMessageSerde() {
            super(new EventMessageSerializer(), new EventMessageDeserializer());
        }

    }

    static public <T> Serde<T> serdeFrom(Class<T> type) {
        if (EventMessage.class.isAssignableFrom(type)) {
            return (Serde<T>) new EventMessageSerde();
        }

        return VertxSerdes.serdeFrom(type);
    }

}
