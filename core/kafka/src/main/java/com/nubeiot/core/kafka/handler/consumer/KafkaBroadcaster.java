package com.nubeiot.core.kafka.handler.consumer;

import java.util.Objects;
import java.util.function.Function;

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
 * @see KafkaConsumerHandler
 * @see EventMessage
 * @see EventController
 */
public final class KafkaBroadcaster<K, V, T extends KafkaBroadcasterTransformer<K, V>>
    extends KafkaConsumerHandler<K, V, EventMessage, T> {

    public static final String EVENTBUS_CONTROLLER = "EVENTBUS_CONTROLLER";
    private KafkaBroadcasterTransformer<K, V> transformer = new KafkaBroadcasterTransformer<>();
    private final EventModel model;

    public KafkaBroadcaster(@NonNull Function<String, Object> sharedDataFunction, @NonNull EventModel model) {
        super(sharedDataFunction);
        if (model.getPattern() == EventPattern.REQUEST_RESPONSE) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT,
                                    Strings.format("Does not supported {1} in {2}", EventPattern.REQUEST_RESPONSE,
                                                   KafkaBroadcaster.class.getSimpleName()));
        }
        this.model = model;
    }

    @Override
    protected void execute(EventMessage result) {
        EventController controller = (EventController) sharedDataFunc.apply(EVENTBUS_CONTROLLER);
        controller.fire(model.getAddress(), model.getPattern(), result);
    }

    @Override
    protected T transformer() {
        return (T) transformer;
    }

    @Override
    public KafkaConsumerHandler registerTransformer(T transformer) {
        if (Objects.nonNull(transformer)) {
            this.transformer = transformer;
        }
        return this;
    }

}
