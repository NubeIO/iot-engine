package com.nubeiot.core.http.client;

import java.util.Objects;
import java.util.function.Supplier;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ClientDelegate implements Supplier<HttpClient> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @NonNull
    private final HttpClient client;
    @Getter
    private final HttpClientConfig config;
    protected Handler<Void> endHandler;

    protected ClientDelegate(@NonNull HttpClient client) {
        this.client = client;
        this.config = new HttpClientConfig(((HttpClientImpl) client).getOptions());
    }

    protected ClientDelegate(@NonNull Vertx vertx, HttpClientConfig config) {
        this.config = Objects.isNull(config) ? new HttpClientConfig() : config;
        this.client = vertx.createHttpClient(this.config.getOptions());
    }

    static RequestOptions evaluateRequestOpts(HttpClientConfig clientConfig, RequestOptions options) {
        if (Objects.isNull(options)) {
            final HttpClientOptions opt = clientConfig.getOptions();
            return new RequestOptions().setHost(opt.getDefaultHost()).setPort(opt.getDefaultPort()).setSsl(opt.isSsl());
        }
        return options;
    }

    static RequestOptions evaluateRequestOpts(HttpClientConfig config, String path) {
        return evaluateRequestOpts(config, path, (RequestOptions) null);
    }

    static RequestOptions evaluateRequestOpts(HttpClientConfig config, String path, RequestOptions options) {
        RequestOptions opt = evaluateRequestOpts(config, options);
        if (Strings.isNotBlank(path)) {
            return opt.setURI(path);
        }
        return opt;
    }

    static RequestOptions evaluateRequestOpts(HttpClientConfig config, String path, RequestData requestData) {
        return evaluateRequestOpts(config, (RequestOptions) null).setURI(computePath(path, requestData));
    }

    private static String computePath(String path, RequestData requestData) {
        if (Strings.isBlank(path)) {
            return "";
        }
        return path;
    }

    @Override
    public final HttpClient get() {
        return client;
    }

}
