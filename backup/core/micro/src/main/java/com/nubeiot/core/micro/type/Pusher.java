package com.nubeiot.core.micro.type;

import java.util.function.Consumer;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.event.ReplyEventHandler;
import com.nubeiot.core.http.base.event.EventMethodDefinition;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class Pusher implements EventMessagePusher {

    @NonNull
    private final EventbusClient controller;
    @NonNull
    private final EventMethodDefinition definition;
    @NonNull
    private final DeliveryOptions options;
    @NonNull
    private final String address;

    @Override
    public void execute(String path, HttpMethod httpMethod, JsonObject requestData, Consumer<ResponseData> dataConsumer,
                        Consumer<Throwable> errorConsumer) {
        EventAction action = definition.search(path, httpMethod);
        ReplyEventHandler handler = ReplyEventHandler.builder().system("SERVICE_DISCOVERY").address(address)
                                                     .action(EventAction.RETURN)
                                                     .success(msg -> dataConsumer.accept(ResponseData.from(msg)))
                                                     .exception(errorConsumer)
                                                     .build();
        controller.request(address, EventMessage.initial(action, requestData), handler, options);
    }

}
