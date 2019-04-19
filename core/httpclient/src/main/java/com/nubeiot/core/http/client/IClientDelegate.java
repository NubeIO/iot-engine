package com.nubeiot.core.http.client;

import java.util.function.Supplier;

import com.nubeiot.core.http.base.HostInfo;

import io.vertx.core.http.HttpClient;

interface IClientDelegate extends Supplier<HttpClient> {

    /**
     * @return client agent
     */
    String getAgent();

    /**
     * @return handler config
     */
    HttpClientConfig.HandlerConfig getHandlerConfig();

    /**
     * @return HostInfo
     */
    HostInfo getHostInfo();

    /**
     * Close client
     */
    void close();

}
