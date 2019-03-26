package com.nubeiot.core.http.client;

import java.util.Objects;

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.client.handler.ClientEndHandler;
import com.nubeiot.core.http.client.handler.ClientErrorHandler;
import com.nubeiot.core.http.client.handler.HttpClientWriter;
import com.nubeiot.core.http.client.handler.LightweightResponseHandler;

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
    public Single<ResponseData> execute(RequestOptions options, HttpMethod method, RequestData requestData,
                                        boolean swallowError) {
        RequestData reqData = decorator(requestData);
        return Single.create(emitter -> {
            LightweightResponseHandler responseHandler = new LightweightResponseHandler<>(
                getConfig().getLightweightBodyHandlerClass(), emitter, swallowError);
            ClientErrorHandler exceptionHandler = ClientErrorHandler.create(emitter,
                                                                            getConfig().getErrorHandlerClass());
            ClientEndHandler endHandler = ClientEndHandler.create(get(), getConfig().getEndHandlerClass());
            HttpClientRequest request = get().request(method, evaluateRequestOpts(options), responseHandler)
                                             .exceptionHandler(exceptionHandler)
                                             .endHandler(
                                                 Objects.nonNull(this.endHandler) ? this.endHandler : endHandler);
            logger.info("Make HTTP request {}::{} | <{}> | <{}>", request.method(), request.absoluteURI(),
                        reqData.toJson());
            HttpClientWriter.create(getConfig().getClientWriterClass()).apply(request, reqData).end();
        });
    }

    @Override
    public Single<ResponseData> upload(RequestOptions options, String uploadFile) {
        return null;
    }

    @Override
    public Single<ResponseData> push(RequestOptions options, ReadStream readStream, HttpMethod method) {
        RequestOptions opts = evaluateRequestOpts(options);
        return null;
    }

    @Override
    public Single<AsyncFile> download(RequestOptions options, AsyncFile saveFile) {
        RequestOptions opts = evaluateRequestOpts(options);
        return null;
    }

    @Override
    public Single<WriteStream> pull(RequestOptions options, WriteStream writeStream) {
        RequestOptions opts = evaluateRequestOpts(options);
        return null;
    }

    @Override
    public HttpClientDelegate overrideEndHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;
        return this;
    }

    private RequestData decorator(RequestData requestData) {
        RequestData reqData = Objects.isNull(requestData) ? RequestData.builder().build() : requestData;
        final JsonObject headers = reqData.headers();
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE.toString())) {
            headers.put(HttpHeaders.CONTENT_TYPE.toString(), HttpUtils.DEFAULT_CONTENT_TYPE);
        }
        if (!headers.containsKey(HttpHeaders.USER_AGENT.toString())) {
            headers.put(HttpHeaders.USER_AGENT.toString(), getConfig().getUserAgent());
        }
        return reqData;
    }

    private RequestOptions evaluateRequestOpts(RequestOptions options) {
        if (Objects.isNull(options)) {
            return new RequestOptions().setHost(getConfig().getOptions().getDefaultHost())
                                       .setPort(getConfig().getOptions().getDefaultPort())
                                       .setSsl(getConfig().getOptions().isSsl());
        }
        return options;
    }

}
