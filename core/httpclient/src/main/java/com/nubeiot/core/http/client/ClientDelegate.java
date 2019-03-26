package com.nubeiot.core.http.client;

import java.util.Objects;
import java.util.function.Supplier;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

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

    @Override
    public final HttpClient get() {
        return client;
    }

}
