package com.nubeiot.core.kafka.handler.consumer;

import io.vertx.core.Vertx;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

/**
 * Responsible for broadcasting data via {@code eventbus} after receiving data from {@code Kafka Consumer}
 *
 * @see AbstractKafkaConsumerHandler
 * @see EventMessage
 * @see EventController
 */
public final class KafkaBroadcaster<K, V, T extends KafkaBroadcasterTransformer<K, V>>
    extends AbstractKafkaConsumerHandler<K, V, T, EventMessage> {

    private final EventModel model;

    @SuppressWarnings("unchecked")
    KafkaBroadcaster(Vertx vertx, @NonNull EventModel model, String sharedKey) {
        super(vertx);
        if (model.getPattern() == EventPattern.REQUEST_RESPONSE) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT,
                                    Strings.format("Does not supported {0} in {1}", EventPattern.REQUEST_RESPONSE,
                                                   KafkaBroadcaster.class.getSimpleName()));
        }
        this.model = model;
        register((T) KafkaBroadcasterTransformer.DEFAULT, sharedKey);
    }

    @Override
    public void execute(EventMessage result) {
        EventController controller = getSharedDataValue(SharedDataDelegate.SHARED_EVENTBUS);
        controller.fire(model.getAddress(), model.getPattern(), result, null);
    }

}
