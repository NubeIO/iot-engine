package com.nubeiot.core.http.client;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.TimeoutException;
import com.nubeiot.core.http.base.event.WebsocketClientEventMetadata;
import com.nubeiot.core.http.client.handler.WebsocketClientWriter;
import com.nubeiot.core.http.client.handler.WsLightResponseDispatcher;
import com.nubeiot.core.http.client.handler.WsResponseErrorHandler;

class WebsocketClientDelegateImpl extends ClientDelegate implements WebsocketClientDelegate {

    private Vertx vertx;

    WebsocketClientDelegateImpl(Vertx vertx, HttpClientConfig config) {
        super(vertx, config);
        this.vertx = vertx;
    }

    @Override
    public void open(WebsocketClientEventMetadata metadata, RequestOptions options, MultiMap headers) {
        EventController controller = new EventController(vertx);
        RequestOptions opts = evaluateRequestOpts(getConfig(), metadata.getPath(), options);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> ref = new AtomicReference<>();
        get().websocket(opts, headers, ws -> {
            latch.countDown();
            logger.info("Websocket to {} is connected", JsonObject.mapFrom(opts).encode());
            controller.register(metadata.getPublisher(), new WebsocketClientWriter(ws, metadata.getPublisher()));
            EventModel listener = metadata.getListener();
            ws.handler(WsLightResponseDispatcher.create(controller, listener, WsLightResponseDispatcher.class));
            ws.exceptionHandler(WsResponseErrorHandler.create(controller, listener, WsResponseErrorHandler.class));
        }, t -> {
            ref.set(t);
            latch.countDown();
        });
        try {
            boolean r = latch.await(getConfig().getOptions().getConnectTimeout() + 100, TimeUnit.MILLISECONDS);
            final Throwable error = ref.get();
            if (r && Objects.isNull(error)) {
                return;
            }
            if (!r || (error instanceof VertxException && error.getMessage().equals("Connection was closed"))) {
                throw new TimeoutException("Request timeout", error);
            }
            throw new HttpException("Failed when open websocket connection", error);
        } catch (InterruptedException e) {
            throw new InitializerError("Interrupted thread when open websocket connection", e);
        }
    }

    @Override
    public void asyncOpen(WebsocketClientEventMetadata metadata, MultiMap headers) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void close() {

    }

}
