package com.nubeiot.core.http.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.ServiceException;
import com.nubeiot.core.http.WebsocketEventMetadata;
import com.nubeiot.core.http.utils.RequestConverter;
import com.nubeiot.core.utils.Strings;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.reactivex.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSSocket;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Websocket event bus handler
 */
public class WebsocketEventHandler implements Handler<BridgeEvent> {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketEventHandler.class);
    private final EventController eventController;
    private final Map<String, WebsocketEventMetadata> metadataByListener = new HashMap<>();

    public WebsocketEventHandler(@NonNull EventController eventController,
                                 @NonNull List<WebsocketEventMetadata> addressMap) {
        this.eventController = eventController;
        addressMap.forEach(metadata -> metadataByListener.put(metadata.getListener().getAddress(), metadata));
    }

    @Override
    public void handle(BridgeEvent event) {
        if (event.type() == BridgeEventType.SEND) {
            clientToServer(event);
        } else {
            logEvent(event, true);
        }
        if (!event.isComplete()) {
            event.complete(true);
        }
    }

    private void logEvent(BridgeEvent event, boolean debug) {
        String msg = "Websocket::Event: {} - Remote: {} - Path: {} - Id: {}";
        SockJSSocket socket = event.socket();
        if (debug) {
            logger.debug(msg, event.type(), socket.remoteAddress(), socket.uri(), socket.writeHandlerID());
        } else {
            logger.info(msg, event.type(), socket.remoteAddress(), socket.uri(), socket.writeHandlerID());
        }
    }

    protected void clientToServer(BridgeEvent event) {
        logEvent(event, false);
        event.socket().exceptionHandler(Throwable::printStackTrace);
        String address = event.getRawMessage().getString("address");
        WebsocketEventMetadata metadata = this.metadataByListener.get(address);
        if (Objects.isNull(metadata)) {
            String error = Strings.format("Address {0} is not found", address);
            //            event.socket().close(HttpResponseStatus.NOT_FOUND.code(), error);
            event.fail(error);
            return;
        }
        try {
            EventMessage raw = EventMessage.from(event.getRawMessage().getValue("body"));
            EventMessage msg = EventMessage.success(raw.getAction(), RequestConverter.convert(raw, event.socket()));
            logger.info("Websocket::Client message: {}", msg.toJson().encode());
            eventController.request(metadata.getProcessor().getAddress(), metadata.getProcessor().getPattern(), msg,
                                    new WebsocketEventResponseHandler(eventController, raw.getAction(), metadata));
        } catch (NubeException e) {
            //            event.fail(e);
            //            event.socket().end(Buffer.buffer("halo halo"));
            //            event.socket().close(HttpStatusMapping.error(HttpMethod.CONNECT, e).code(), e.getMessage());
            eventController.request(metadata.getPublisher().getAddress(), EventPattern.POINT_2_POINT,
                                    EventMessage.error(EventAction.RETURN, e));
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    class WebsocketEventResponseHandler implements Consumer<AsyncResult<io.vertx.core.eventbus.Message<Object>>> {

        @NonNull
        final EventController eventController;
        @NonNull
        final EventAction action;
        @NonNull
        final WebsocketEventMetadata metadata;

        @Override
        public void accept(AsyncResult<io.vertx.core.eventbus.Message<Object>> reply) {
            handleEventReply(reply).subscribe(this::handleReplySuccess, this::handleReplyError);
        }

        private Single<EventMessage> handleEventReply(AsyncResult<Message<Object>> reply) {
            if (reply.failed()) {
                String msg = Strings.format("No reply action {0} from \"{1}\"", action,
                                            metadata.getProcessor().getAddress());
                HiddenException hidden = new HiddenException(NubeException.ErrorCode.EVENT_ERROR, msg, reply.cause());
                return Single.error(new ServiceException("Service unavailable", hidden));
            }
            return Single.just(EventMessage.from(reply.result().body()));
        }

        private void handleReplySuccess(EventMessage eventMessage) {
            logger.info("Websocket::Backend eventbus data: {}", eventMessage.toJson().encode());
            EventModel publisher = metadata.getPublisher();
            eventController.response(publisher.getAddress(), publisher.getPattern(), eventMessage);
        }

        private void handleReplyError(Throwable throwable) {
            logger.error("Websocket::Backend eventbus error", throwable);
            EventModel publisher = metadata.getPublisher();
            eventController.response(publisher.getAddress(), publisher.getPattern(), ErrorMessage.parse(throwable));
        }

    }

}
