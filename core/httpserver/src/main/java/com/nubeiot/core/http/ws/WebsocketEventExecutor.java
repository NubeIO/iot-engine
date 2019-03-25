package com.nubeiot.core.http.ws;

import java.util.Objects;
import java.util.function.Consumer;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.ReplyEventHandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WebsocketEventExecutor {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketEventExecutor.class);
    private final EventController controller;

    public void execute(@NonNull WebsocketEventMessage socketMessage, @NonNull WebsocketEventMetadata metadata,
                        @NonNull Consumer<EventMessage> callback) {
        EventMessage msg = EventMessage.success(socketMessage.getBody().getAction(),
                                                RequestData.from(socketMessage.getBody()));
        logger.info("WEBSOCKET::Client Request: {}", msg.toJson().encode());
        EventModel processor = metadata.getProcessor();
        if (processor.getPattern() == EventPattern.REQUEST_RESPONSE) {
            ReplyEventHandler replyConsumer = new ReplyEventHandler("WEBSOCKET", msg.getAction(),
                                                                    processor.getAddress(),
                                                                    callback(metadata.getPublisher(), callback));
            controller.request(processor.getAddress(), processor.getPattern(), msg, replyConsumer);
        } else {
            controller.request(processor.getAddress(), processor.getPattern(), msg);
            callback.accept(EventMessage.success(EventAction.RETURN));
        }
    }

    private Consumer<EventMessage> callback(EventModel publisher, Consumer<EventMessage> defCallback) {
        if (Objects.isNull(publisher)) {
            return defCallback;
        }
        return eventMessage -> controller.response(publisher.getAddress(), publisher.getPattern(), eventMessage);
    }

}
