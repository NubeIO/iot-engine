package com.nubeiot.core.http.client;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.base.event.WebsocketClientEventMetadata;
import com.nubeiot.core.http.client.HttpClientConfig.HandlerConfig;
import com.nubeiot.core.http.client.handler.WebsocketClientWriter;
import com.nubeiot.core.http.client.handler.WsConnectErrorHandler;
import com.nubeiot.core.http.client.handler.WsLightResponseDispatcher;
import com.nubeiot.core.http.client.handler.WsResponseErrorHandler;

class WebsocketClientDelegateImpl extends ClientDelegate implements WebsocketClientDelegate {

    private final int connTimeout;
    private final EventController controller;

    WebsocketClientDelegateImpl(Vertx vertx, HttpClientConfig config) {
        super(vertx, config);
        this.connTimeout = config.getOptions().getConnectTimeout();
        this.controller = SharedDataDelegate.getEventController(vertx, WebsocketClientDelegate.class.getName());
    }

    @Override
    public void open(WebsocketClientEventMetadata metadata, MultiMap headers) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> ref = new AtomicReference<>();
        HandlerConfig handler = getHandlerConfig();
        get().websocket(metadata.getPath(), headers, ws -> {
            latch.countDown();
            logger.info("Websocket to {} is connected", getHostInfo().toJson());
            controller.register(metadata.getPublisher(), new WebsocketClientWriter(ws, metadata.getPublisher()));
            EventModel listener = metadata.getListener();
            ws.handler(WsLightResponseDispatcher.create(controller, listener, getHostInfo(),
                                                        handler.getWsLightResponseHandlerClass()));
            ws.exceptionHandler(WsResponseErrorHandler.create(controller, listener, handler.getWsErrorHandlerClass()));
        }, t -> {
            ref.set(t);
            latch.countDown();
        });
        try {
            boolean r = latch.await(connTimeout + 100, TimeUnit.MILLISECONDS);
            final Throwable error = ref.get();
            if (r && Objects.isNull(error)) {
                return;
            }
            WsConnectErrorHandler.create(getHostInfo(), controller, handler.getWsConnectErrorHandlerClass())
                                 .handle(error);
        } catch (InterruptedException e) {
            throw new InitializerError("Interrupted thread when open websocket connection", e);
        }
    }

    @Override
    public void asyncOpen(WebsocketClientEventMetadata metadata, MultiMap headers) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public EventController getEventController() {
        return controller;
    }

}
