package com.nubeiot.core.http.client;

import io.vertx.core.http.HttpClientOptions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.http.client.handler.HttpClientEndHandler;
import com.nubeiot.core.http.client.handler.HttpClientErrorHandler;
import com.nubeiot.core.http.client.handler.HttpClientWriter;
import com.nubeiot.core.http.client.handler.HttpLightResponseBodyHandler;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class HttpClientConfig implements IConfig {

    private String userAgent = "nubeio.httpclient";
    private HttpClientHandlerConfig handlerConfig = HttpClientHandlerConfig.builder().build();
    private HttpClientOptions options;

    HttpClientConfig() {
        this(new HttpClientOptions());
    }

    HttpClientConfig(@NonNull HttpClientOptions options) {
        this.options = options.setIdleTimeout(30)
                              .setTryUseCompression(true)
                              .setTryUsePerFrameWebsocketCompression(true)
                              .setTryUsePerMessageWebsocketCompression(true);
    }

    @Override
    public String name() { return "__httpClient__"; }

    @Override
    public Class<? extends IConfig> parent() { return AppConfig.class; }

    @Builder(builderClassName = "Builder")
    @JsonDeserialize(builder = HttpClientHandlerConfig.Builder.class)
    public static class HttpClientHandlerConfig {

        @Default
        private final String clientWriterClass = HttpClientWriter.class.getName();
        @Default
        private final String errorHandlerClass = HttpClientErrorHandler.class.getName();
        @Default
        private final String endHandlerClass = HttpClientEndHandler.class.getName();
        @Default
        private final String lightBodyHandlerClass = HttpLightResponseBodyHandler.class.getName();

        public Class<? extends HttpClientWriter> getClientWriterClass() {
            return Strings.isBlank(clientWriterClass)
                   ? HttpClientWriter.class
                   : ReflectionClass.findClass(clientWriterClass);
        }

        public Class<? extends HttpClientErrorHandler> getErrorHandlerClass() {
            return Strings.isBlank(errorHandlerClass)
                   ? HttpClientErrorHandler.class
                   : ReflectionClass.findClass(errorHandlerClass);
        }

        public Class<? extends HttpClientEndHandler> getEndHandlerClass() {
            return Strings.isBlank(endHandlerClass)
                   ? HttpClientEndHandler.class
                   : ReflectionClass.findClass(endHandlerClass);
        }

        public Class<? extends HttpLightResponseBodyHandler> getLightBodyHandlerClass() {
            return Strings.isBlank(lightBodyHandlerClass)
                   ? HttpLightResponseBodyHandler.class
                   : ReflectionClass.findClass(lightBodyHandlerClass);
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {}

    }

}
