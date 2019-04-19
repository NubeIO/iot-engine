package com.nubeiot.core.http.client;

import java.util.Objects;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.utils.Strings;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class ClientDelegate implements IClientDelegate {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @NonNull
    private final HttpClient client;
    @Getter
    private final String agent;
    @Getter
    private final HttpClientConfig.HandlerConfig handlerConfig;
    @Getter
    private final HostInfo hostInfo;
    protected Handler<Void> endHandler;

    protected ClientDelegate(@NonNull HttpClient client) {
        HttpClientConfig config = new HttpClientConfig(((HttpClientImpl) client).getOptions());
        this.agent = config.getUserAgent();
        this.handlerConfig = config.getHandlerConfig();
        this.hostInfo = HostInfo.builder()
                                .host(config.getOptions().getDefaultHost())
                                .port(config.getOptions().getDefaultPort())
                                .ssl(config.getOptions().isSsl())
                                .build();
        this.client = client;
    }

    protected ClientDelegate(@NonNull Vertx vertx, @NonNull HttpClientConfig config) {
        this.agent = config.getUserAgent();
        this.handlerConfig = config.getHandlerConfig();
        this.hostInfo = config.getHostInfo();
        this.client = vertx.createHttpClient(config.getOptions());
    }

    static HostInfo evaluateRequestOpts(HttpClientConfig clientConfig, HostInfo options) {
        if (Objects.isNull(options)) {
            return clientConfig.getHostInfo();
        }
        return JsonData.from(options.toJson().mergeIn(clientConfig.getHostInfo().toJson(), true), HostInfo.class);
    }

    static RequestOptions evaluateRequestOpts(HostInfo hostInfo, String path) {
        return new RequestOptions().setHost(hostInfo.getHost())
                                   .setPort(hostInfo.getPort())
                                   .setSsl(hostInfo.isSsl())
                                   .setURI(path);
    }

    private static String computePath(String path, RequestData requestData) {
        if (Strings.isBlank(path)) {
            return "";
        }
        return path;
    }

    static HttpClientConfig cloneConfig(@NonNull HttpClientConfig config, HostInfo hostInfo, int idleTimeout) {
        HostInfo info = evaluateRequestOpts(config, hostInfo);
        HttpClientOptions clientOpts = new HttpClientOptions(config.getOptions()).setIdleTimeout(idleTimeout)
                                                                                 .setSsl(info.isSsl())
                                                                                 .setDefaultHost(info.getHost())
                                                                                 .setDefaultPort(info.getPort());
        return IConfig.merge(config, new JsonObject().put("options", clientOpts.toJson()), HttpClientConfig.class);
    }

    @Override
    public final HttpClient get() {
        return client;
    }

    @Override
    public void close() {
        try {
            get().close();
        } catch (IllegalStateException e) {
            logger.warn(e.getMessage());
        }
    }

}
