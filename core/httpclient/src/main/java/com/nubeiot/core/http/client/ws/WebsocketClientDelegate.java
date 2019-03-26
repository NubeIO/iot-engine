package com.nubeiot.core.http.client.ws;

import java.util.function.Supplier;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.RequestOptions;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.client.HttpClientConfig;

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
     * Send data to websocket server
     *
     * @return single response data. Must be subscribe before using
     */
    Single<ResponseData> send(RequestData requestData);

    /**
     * Send data to websocket server
     *
     * @param options Request options. Override default server host and port
     * @param data    Request data
     * @return single response data. Must be subscribe before using
     */
    Single<ResponseData> send(RequestOptions options, RequestData data);

    /**
     * Broadcast data to all websocket client
     *
     * @param requestData
     * @return
     */
    Single<ResponseData> broadcast(RequestData requestData);

}
