package com.nubeiot.core.http.client;

import io.vertx.core.http.HttpClientOptions;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.http.client.handler.ClientEndHandler;
import com.nubeiot.core.http.client.handler.ClientErrorHandler;
import com.nubeiot.core.http.client.handler.HttpClientWriter;
import com.nubeiot.core.http.client.handler.LightweightResponseBodyHandler;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public class HttpClientConfig implements IConfig {

    private String userAgent = "nubeio.httpclient";
    @Setter
    private String clientWriterClass = HttpClientWriter.class.getName();
    @Setter
    private String errorHandlerClass = ClientErrorHandler.class.getName();
    @Setter
    private String endHandlerClass = ClientEndHandler.class.getName();
    @Setter
    private String lightweightBodyHandlerClass = LightweightResponseBodyHandler.class.getName();
    private HttpClientOptions options;

    HttpClientConfig() {
        this(new HttpClientOptions().setIdleTimeout(30)
                                    .setTryUseCompression(true)
                                    .setTryUsePerFrameWebsocketCompression(true)
                                    .setTryUsePerMessageWebsocketCompression(true));
    }

    HttpClientConfig(@NonNull HttpClientOptions options) {
        this.options = options.setIdleTimeout(30)
                              .setTryUseCompression(true)
                              .setTryUsePerFrameWebsocketCompression(true)
                              .setTryUsePerMessageWebsocketCompression(true);
    }

    public Class<? extends HttpClientWriter> getClientWriterClass() {
        return Strings.isBlank(clientWriterClass)
               ? HttpClientWriter.class
               : ReflectionClass.findClass(clientWriterClass);
    }

    public Class<? extends ClientErrorHandler> getErrorHandlerClass() {
        return Strings.isBlank(errorHandlerClass)
               ? ClientErrorHandler.class
               : ReflectionClass.findClass(errorHandlerClass);
    }

    public Class<? extends ClientEndHandler> getEndHandlerClass() {
        return Strings.isBlank(endHandlerClass) ? ClientEndHandler.class : ReflectionClass.findClass(endHandlerClass);
    }

    public Class<? extends LightweightResponseBodyHandler> getLightweightBodyHandlerClass() {
        return Strings.isBlank(lightweightBodyHandlerClass)
               ? LightweightResponseBodyHandler.class
               : ReflectionClass.findClass(lightweightBodyHandlerClass);
    }

    @Override
    public String name() { return "__httpClient__"; }

    @Override
    public Class<? extends IConfig> parent() { return AppConfig.class; }

}
