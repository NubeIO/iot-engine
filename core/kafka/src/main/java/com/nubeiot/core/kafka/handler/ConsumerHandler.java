package com.nubeiot.core.kafka.handler;

import java.util.function.Consumer;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;

import lombok.Builder;
import lombok.NonNull;

@Builder(builderClassName = "Builder")
public class ConsumerHandler implements Consumer<EventMessage> {

    @NonNull
    private final EventModel model;
    @NonNull
    private final EventController controller;

    @Override
    public void accept(EventMessage message) {
        controller.fire(model.getAddress(), model.getPattern(), message);
    }

}
