package com.nubeiot.core.http.client;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.vertx.core.http.HttpClientOptions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.http.client.handler.HttpClientWriter;
import com.nubeiot.core.http.client.handler.HttpErrorHandler;
import com.nubeiot.core.http.client.handler.HttpHeavyResponseHandler;
import com.nubeiot.core.http.client.handler.HttpLightResponseBodyHandler;
import com.nubeiot.core.http.client.handler.WsConnectErrorHandler;
import com.nubeiot.core.http.client.handler.WsLightResponseDispatcher;
import com.nubeiot.core.http.client.handler.WsResponseErrorHandler;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
public final class HttpClientConfig implements IConfig {

    public static final int CONNECT_TIMEOUT_SECOND = 45;
    public static final int HTTP_IDLE_TIMEOUT_SECOND = 15;
    public static final int WS_IDLE_TIMEOUT_SECOND = 1200;
    private String userAgent = "nubeio.httpclient";
    private HostInfo hostInfo;
    private HttpClientOptions options;
    private HandlerConfig handlerConfig = new HandlerConfig();

    HttpClientConfig() {
        this(new HttpClientOptions().setIdleTimeout(HTTP_IDLE_TIMEOUT_SECOND)
                                    .setIdleTimeoutUnit(TimeUnit.SECONDS)
                                    .setConnectTimeout(45000)
                                    .setTryUseCompression(true)
                                    .setWebsocketCompressionAllowClientNoContext(true)
                                    .setWebsocketCompressionRequestServerNoContext(true)
                                    .setWebsocketCompressionLevel(6)
                                    .setTryUsePerFrameWebsocketCompression(false)
                                    .setTryUsePerMessageWebsocketCompression(true));
    }

    HttpClientConfig(@NonNull HttpClientOptions options) {
        this.options = options;
    }

    @Override
    public String name() { return "__httpClient__"; }

    @Override
    public Class<? extends IConfig> parent() { return AppConfig.class; }

    public HostInfo getHostInfo() {
        if (Objects.nonNull(hostInfo)) {
            return hostInfo;
        }
        return HostInfo.builder()
                       .host(this.getOptions().getDefaultHost())
                       .port(this.getOptions().getDefaultPort())
                       .ssl(this.getOptions().isSsl())
                       .build();
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class HandlerConfig {

        private Class<? extends HttpClientWriter> httpClientWriterClass = HttpClientWriter.class;
        private Class<? extends HttpLightResponseBodyHandler> httpLightBodyHandlerClass
            = HttpLightResponseBodyHandler.class;
        private Class<? extends HttpHeavyResponseHandler> httpHeavyBodyHandlerClass = HttpHeavyResponseHandler.class;
        private Class<? extends HttpErrorHandler> httpErrorHandlerClass = HttpErrorHandler.class;
        private Class<? extends WsConnectErrorHandler> wsConnectErrorHandlerClass = WsConnectErrorHandler.class;
        private Class<? extends WsResponseErrorHandler> wsErrorHandlerClass = WsResponseErrorHandler.class;
        private Class<? extends WsLightResponseDispatcher> wsLightResponseHandlerClass
            = WsLightResponseDispatcher.class;

        @JsonCreator
        HandlerConfig(@JsonProperty("httpClientWriterClass") String httpClientWriterClass,
                      @JsonProperty("httpLightBodyHandlerClass") String httpLightBodyHandlerClass,
                      @JsonProperty("httpHeavyBodyHandlerClass") String httpHeavyBodyHandlerClass,
                      @JsonProperty("httpErrorHandlerClass") String httpErrorHandlerClass,
                      @JsonProperty("wsConnectErrorHandlerClass") String wsConnectErrorHandlerClass,
                      @JsonProperty("wsErrorHandlerClass") String wsErrorHandlerClass,
                      @JsonProperty("wsLightResponseHandlerClass") String wsLightResponseHandlerClass) {
            this.httpClientWriterClass = Strings.isBlank(httpClientWriterClass)
                                         ? HttpClientWriter.class
                                         : ReflectionClass.findClass(httpClientWriterClass);
            this.httpLightBodyHandlerClass = Strings.isBlank(httpLightBodyHandlerClass)
                                             ? HttpLightResponseBodyHandler.class
                                             : ReflectionClass.findClass(httpLightBodyHandlerClass);
            this.httpHeavyBodyHandlerClass = Strings.isBlank(httpHeavyBodyHandlerClass)
                                             ? HttpHeavyResponseHandler.class
                                             : ReflectionClass.findClass(httpHeavyBodyHandlerClass);
            this.httpErrorHandlerClass = Strings.isBlank(httpErrorHandlerClass)
                                         ? HttpErrorHandler.class
                                         : ReflectionClass.findClass(httpErrorHandlerClass);
            this.wsConnectErrorHandlerClass = Strings.isBlank(wsConnectErrorHandlerClass)
                                              ? WsConnectErrorHandler.class
                                              : ReflectionClass.findClass(wsConnectErrorHandlerClass);
            this.wsErrorHandlerClass = Strings.isBlank(wsErrorHandlerClass)
                                       ? WsResponseErrorHandler.class
                                       : ReflectionClass.findClass(wsErrorHandlerClass);
            this.wsLightResponseHandlerClass = Strings.isBlank(wsLightResponseHandlerClass)
                                               ? WsLightResponseDispatcher.class
                                               : ReflectionClass.findClass(wsLightResponseHandlerClass);
        }

    }

}
