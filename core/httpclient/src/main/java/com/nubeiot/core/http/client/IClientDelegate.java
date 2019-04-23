package com.nubeiot.core.http.client;

import java.util.function.Supplier;

import io.vertx.core.http.HttpClient;

import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.http.client.HttpClientConfig.HandlerConfig;

interface IClientDelegate extends Supplier<HttpClient> {

    /**
     * @return client HTTP agent
     */
    String getUserAgent();

    /**
     * @return handler config
     */
    HandlerConfig getHandlerConfig();

    /**
     * @return HostInfo
     */
    HostInfo getHostInfo();

    /**
     * Close client
     */
    void close();

}
