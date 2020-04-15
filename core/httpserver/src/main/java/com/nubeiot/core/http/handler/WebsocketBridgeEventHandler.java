package com.nubeiot.core.http.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import io.github.zero.utils.Strings;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.base.event.WebsocketServerEventMetadata;
import com.nubeiot.core.http.ws.WebsocketEventExecutor;
import com.nubeiot.core.http.ws.WebsocketEventMessage;

import lombok.NonNull;

/**
 * Websocket event bus handler
 */
//TODO handle auth with socket header
public class WebsocketBridgeEventHandler implements Handler<BridgeEvent> {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketBridgeEventHandler.class);
    private final Map<String, WebsocketServerEventMetadata> metadataByListener = new HashMap<>();
    private final WebsocketEventExecutor executor;

    public WebsocketBridgeEventHandler(@NonNull EventbusClient eventbusClient,
                                       @NonNull List<WebsocketServerEventMetadata> addressMap) {
        this.executor = new WebsocketEventExecutor(eventbusClient);
        addressMap.forEach(this::initMetadata);
    }

    private void initMetadata(WebsocketServerEventMetadata metadata) {
        EventModel eventModel = Objects.isNull(metadata.getListener())
                                ? metadata.getPublisher()
                                : metadata.getListener();
        metadataByListener.put(eventModel.getAddress(), metadata);
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

    private void clientToServer(BridgeEvent event) {
        logEvent(event, false);
        SockJSSocket socket = event.socket();
        socket.exceptionHandler(t -> logger.error("WEBSOCKET::Backend error", t));
        String address = event.getRawMessage().getString("address");
        WebsocketServerEventMetadata metadata = this.metadataByListener.get(address);
        if (Objects.isNull(metadata)) {
            String errorMsg = Strings.format("Address {0} is not found", address);
            socket.close(HttpResponseStatus.NOT_FOUND.code(), errorMsg);
            return;
        }
        try {
            logger.info("WEBSOCKET::Redirect message from address: {}", address);
            executor.execute(WebsocketEventMessage.from(event.getRawMessage()), metadata, handleMessage(socket));
        } catch (NubeException e) {
            handleMessage(socket).accept(EventMessage.error(EventAction.RETURN, e));
        }
    }

    //TODO May override consumer for message
    protected Consumer<EventMessage> handleMessage(SockJSSocket socket) {
        return message -> socket.write(message.toJson().encode());
    }

}
