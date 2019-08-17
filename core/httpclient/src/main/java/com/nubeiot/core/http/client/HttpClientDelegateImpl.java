package com.nubeiot.core.http.client;

import static com.nubeiot.core.http.base.HttpUtils.HttpRequests;

import java.util.Objects;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.client.HttpClientConfig.HandlerConfig;
import com.nubeiot.core.http.client.handler.ClientEndHandler;
import com.nubeiot.core.http.client.handler.HttpClientWriter;
import com.nubeiot.core.http.client.handler.HttpErrorHandler;
import com.nubeiot.core.http.client.handler.HttpLightResponseHandler;

import lombok.NonNull;

class HttpClientDelegateImpl extends ClientDelegate implements HttpClientDelegate {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientDelegate.class);

    HttpClientDelegateImpl(@NonNull HttpClient client) {
        super(client);
    }

    HttpClientDelegateImpl(@NonNull Vertx vertx, HttpClientConfig config) {
        super(vertx, config);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Single<ResponseData> execute(String path, HttpMethod method, RequestData requestData, boolean swallowError) {
        RequestData reqData = decorator(requestData);
        return Single.create(emitter -> {
            HandlerConfig config = getHandlerConfig();
            HttpLightResponseHandler responseHandler = new HttpLightResponseHandler<>(
                config.getHttpLightBodyHandlerClass(), emitter, swallowError);
            HttpErrorHandler exceptionHandler = HttpErrorHandler.create(emitter, getHostInfo(),
                                                                        config.getHttpErrorHandlerClass());
            String query = HttpRequests.serializeQuery(reqData.getFilter());
            HttpClientRequest r = get().request(method, path + query, responseHandler)
                                       .exceptionHandler(exceptionHandler)
                                       .endHandler(new ClientEndHandler(getHostInfo(), false));
            if (logger.isDebugEnabled()) {
                logger.debug("Send HTTP request {}::{} | <{}>", r.method(), r.absoluteURI(), reqData.toJson());
            } else {
                logger.info("Send HTTP request {}::{}", r.method(), r.absoluteURI());
            }
            HttpClientWriter.create(config.getHttpClientWriterClass()).apply(r, reqData).end();
        });
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
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE.toString())) {
            headers.put(HttpHeaders.CONTENT_TYPE.toString(), HttpUtils.DEFAULT_CONTENT_TYPE);
        }
        if (!headers.containsKey(HttpHeaders.USER_AGENT.toString())) {
            headers.put(HttpHeaders.USER_AGENT.toString(), this.getUserAgent());
        }
        return reqData;
    }

}
