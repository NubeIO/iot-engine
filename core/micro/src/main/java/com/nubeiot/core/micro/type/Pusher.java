package com.nubeiot.core.micro.type;

import java.util.function.Consumer;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.ReplyEventHandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class Pusher implements EventMessagePusher {

    @NonNull
    private final EventController controller;
    @NonNull
    private final EventMethodDefinition definition;
    @NonNull
    private final DeliveryOptions options;
    @NonNull
    private final String address;
    private final EventPattern pattern;

    @Override
    public void push(String path, HttpMethod httpMethod, RequestData requestData, Consumer<ResponseData> dataConsumer,
                     Consumer<Throwable> errorConsumer) {
        EventAction action = definition.search(path, httpMethod);
        ReplyEventHandler handler = new ReplyEventHandler("SERVICE_DISCOVERY", EventAction.RETURN, path,
                                                          message -> dataConsumer.accept(ResponseData.from(message)),
                                                          errorConsumer);
        controller.request(address, pattern, EventMessage.initial(action, requestData.toJson()), handler);
    }

}
