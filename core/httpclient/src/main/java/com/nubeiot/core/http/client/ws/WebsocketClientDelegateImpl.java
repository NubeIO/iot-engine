package com.nubeiot.core.http.client.ws;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.RequestOptions;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.client.ClientDelegate;
import com.nubeiot.core.http.client.HttpClientConfig;

class WebsocketClientDelegateImpl extends ClientDelegate implements WebsocketClientDelegate {

    WebsocketClientDelegateImpl(HttpClient client) {
        super(client);
    }

    WebsocketClientDelegateImpl(Vertx vertx, HttpClientConfig config) {
        super(vertx, config);
    }

    @Override
    public Single<ResponseData> send(RequestData requestData) {
        return null;
    }

    @Override
    public Single<ResponseData> send(RequestOptions options, RequestData data) {
        return null;
    }

    @Override
    public Single<ResponseData> broadcast(RequestData requestData) {
        return null;
    }

}
