package com.nubeiot.core.http.client;

import java.util.function.Supplier;

import io.vertx.core.http.HttpClient;

interface IClientDelegate extends Supplier<HttpClient> {

    /**
     * @return HttpClientConfig
     */
    HttpClientConfig getConfig();

    void close();

}
