package com.nubeiot.core.http.client;

import static com.nubeiot.core.http.base.HttpUtils.HttpRequests;

import java.util.Objects;

import io.github.zero88.utils.Urls;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.reactivex.RxHelper;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.client.HttpClientConfig.HandlerConfig;
import com.nubeiot.core.http.client.handler.HttpClientWriter;
import com.nubeiot.core.http.client.handler.HttpErrorHandler;
import com.nubeiot.core.http.client.handler.HttpLightResponseHandler;

import lombok.NonNull;

final class HttpClientDelegateImpl extends ClientDelegate implements HttpClientDelegate {

    HttpClientDelegateImpl(@NonNull HttpClient client) {
        super(client);
    }

    HttpClientDelegateImpl(@NonNull Vertx vertx, HttpClientConfig config) {
        super(vertx, config);
    }

    @Override
    public Single<ResponseData> execute(String path, HttpMethod method, RequestData requestData, boolean swallowError) {
        final RequestData reqData = decorator(requestData);
        final HostInfo hostInfo = getHostInfo();
        final HandlerConfig cfg = getHandlerConfig();
        return Single.<ResponseData>create(emitter -> {
            Handler<HttpClientResponse> respHandler = HttpLightResponseHandler.create(emitter, swallowError,
                                                                                      cfg.getHttpLightBodyHandlerClass());
            HttpErrorHandler errHandler = HttpErrorHandler.create(emitter, hostInfo, cfg.getHttpErrorHandlerClass());
            String query = HttpRequests.serializeQuery(reqData.filter());
            HttpClientRequest req = get().request(method, Urls.buildURL(path, query), respHandler)
                                         .exceptionHandler(errHandler);
            if (logger.isDebugEnabled()) {
                logger.debug("Send HTTP request {}::{} | <{}>", req.method(), req.absoluteURI(), reqData.toJson());
            } else {
                logger.info("Send HTTP request {}::{}", req.method(), req.absoluteURI());
            }
            HttpClientWriter.create(cfg.getHttpClientWriterClass()).apply(req, reqData).end();
        }).doOnSuccess(res -> HttpClientRegistry.getInstance().remove(hostInfo, false))
          .doOnError(err -> HttpClientRegistry.getInstance().remove(hostInfo, false))
          .subscribeOn(RxHelper.blockingScheduler(getVertx()));
    }

    @Override
    public Single<ResponseData> upload(String path, String uploadFile) {
        return null;
    }

    @Override
    public Single<ResponseData> push(String path, ReadStream readStream, HttpMethod method) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Single<AsyncFile> download(String path, AsyncFile saveFile) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Single<WriteStream> pull(String path, WriteStream writeStream) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private RequestData decorator(RequestData requestData) {
        RequestData reqData = Objects.isNull(requestData) ? RequestData.builder().build() : requestData;
        final JsonObject headers = reqData.headers();
        if (!headers.containsKey(HttpUtils.NONE_CONTENT_TYPE) &&
            !headers.containsKey(HttpHeaders.CONTENT_TYPE.toString())) {
            headers.put(HttpHeaders.CONTENT_TYPE.toString(), HttpUtils.JSON_CONTENT_TYPE);
        }
        headers.remove(HttpUtils.NONE_CONTENT_TYPE);
        if (!headers.containsKey(HttpHeaders.USER_AGENT.toString())) {
            headers.put(HttpHeaders.USER_AGENT.toString(), this.getUserAgent());
        }
        return reqData;
    }

}
