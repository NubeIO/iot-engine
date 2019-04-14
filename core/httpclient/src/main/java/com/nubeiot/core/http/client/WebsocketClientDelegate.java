package com.nubeiot.core.http.client;

import java.util.function.Supplier;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.RequestOptions;

import com.nubeiot.core.http.base.event.WebsocketClientEventMetadata;

import lombok.NonNull;

public interface WebsocketClientDelegate extends Supplier<HttpClient> {

    static WebsocketClientDelegate create(@NonNull HttpClient client) {
        return new WebsocketClientDelegateImpl(client);
    }

    static WebsocketClientDelegate create(@NonNull Vertx vertx, @NonNull HttpClientConfig config) {
        return new WebsocketClientDelegateImpl(vertx, config);
    }

    /**
     * @return null if use {@link #create(HttpClient)}
     */
    HttpClientConfig getConfig();

    /**
     * Blocking open websocket connection
     *
     * @param metadata Websocket metadata for {@code listener} and {@code publisher}
     * @param headers  Websocket headers
     */
    default void open(@NonNull WebsocketClientEventMetadata metadata, MultiMap headers) {
        open(metadata, ClientDelegate.evaluateRequestOpts(getConfig(), metadata.getPath()), headers);
    }

    /**
     * Blocking open websocket connection
     *
     * @param metadata Websocket metadata for {@code listener} and {@code publisher}
     * @param options  Request Options for override default
     * @param headers  Websocket headers
     */
    void open(@NonNull WebsocketClientEventMetadata metadata, RequestOptions options, MultiMap headers);

    /**
     * Async open websocket connection
     *
     * @param metadata Websocket metadata for {@code listener} and {@code publisher}
     * @param headers  Websocket headers
     */
    void asyncOpen(WebsocketClientEventMetadata metadata, MultiMap headers);

    void close();

}
