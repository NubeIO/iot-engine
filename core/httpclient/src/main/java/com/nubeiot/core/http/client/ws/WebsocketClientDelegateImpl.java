package com.nubeiot.core.http.client.ws;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.RequestOptions;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.base.HttpUtils.HttpHeaderUtils;
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
    public Single<ResponseData> send(RequestOptions options, RequestData data) {
        logger.info(data.headers().encode());
        final HttpClient websocket = get().websocket(options, HttpHeaderUtils.deserializeHeaders(data.headers()),
                                                     ws -> {
                                                         logger.info("Connected");
                                                         ws.handler(buffer -> logger.info("Received msg: {}",
                                                                                          buffer.toString()));
                                                         ws.endHandler(v -> logger.info("End"));
                                                         ws.exceptionHandler(t -> logger.error("Error", t));
                                                         ws.pongHandler(buffer -> logger.info("Pong handler {}",
                                                                                              buffer.toString()));
                                                         ws.writePing(Buffer.buffer("ping"));
                                                         ws.writePong(Buffer.buffer("pong"));
                                                         ws.writeTextMessage(data.body().encode());
                                                         logger.info(ws.localAddress());
                                                         logger.info(ws.remoteAddress());
                                                         logger.info(ws.isSsl());
                                                         logger.info(ws.subProtocol());
                                                         ws.end(Buffer.buffer("fuck end"));
                                                     }, t -> logger.error("Client error", t));
        websocket.connectionHandler(conn -> logger.info("hey"));
        return Single.just(new ResponseData());
    }

    @Override
    public Single<ResponseData> broadcast(RequestData requestData) {
        return null;
    }

}
