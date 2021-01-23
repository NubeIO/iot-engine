package com.nubeiot.core.kafka.serialization;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import io.vertx.kafka.client.serialization.VertxSerdes;

import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;

import lombok.NonNull;

/**
 * Extend {@link VertxSerdes} for factory for creating serializers / deserializers
 *
 * @see VertxSerdes
 * @see Serdes
 */
public final class NubeKafkaSerdes extends Serdes {

    public static final class EventMessageSerde extends WrapperSerde<EventMessage> {

        EventMessageSerde() {
            super(new EventMessageSerializer(), new EventMessageDeserializer());
        }

    }

    @SuppressWarnings("unchecked")
    public static <T> Serde<T> serdeFrom(@NonNull Class<T> type) {
        try {
            if (EventMessage.class.isAssignableFrom(type)) {
                return (Serde<T>) new EventMessageSerde();
            }
            return VertxSerdes.serdeFrom(type);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Unsupported serialize/deserialize type", e);
        }
    }

}
