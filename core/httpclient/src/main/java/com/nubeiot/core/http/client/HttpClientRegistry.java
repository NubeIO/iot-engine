package com.nubeiot.core.http.client;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.nubeiot.core.http.base.HostInfo;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClientRegistry {

    private static HttpClientRegistry instance;
    @Getter(value = AccessLevel.PACKAGE)
    private final Map<HostInfo, HttpClientDelegate> httpRegistries = new ConcurrentHashMap<>();
    @Getter(value = AccessLevel.PACKAGE)
    private final Map<HostInfo, WebsocketClientDelegate> wsRegistries = new ConcurrentHashMap<>();

    public static HttpClientRegistry getInstance() {
        if (Objects.nonNull(instance)) {
            return instance;
        }
        synchronized (HttpClientRegistry.class) {
            if (Objects.nonNull(instance)) {
                return instance;
            }
            instance = new HttpClientRegistry();
            return instance;
        }
    }

    public WebsocketClientDelegate getWebsocket(@NonNull HostInfo hostInfo,
                                                @NonNull Supplier<WebsocketClientDelegate> fallback) {
        return this.wsRegistries.computeIfAbsent(hostInfo, hf -> fallback.get());
    }

    public HttpClientDelegate getHttpClient(@NonNull HostInfo hostInfo,
                                            @NonNull Supplier<HttpClientDelegate> fallback) {
        return this.httpRegistries.computeIfAbsent(hostInfo, hf -> fallback.get());
    }

    public void remove(@NonNull HostInfo hostInfo, boolean isWebsocket) {
        IClientDelegate delegate = isWebsocket
                                   ? this.wsRegistries.remove(hostInfo)
                                   : this.httpRegistries.remove(hostInfo);
        if (Objects.nonNull(delegate)) {
            ((ClientDelegate) delegate).silentClose();
        }
    }

    /**
     * Must be call before closing {@code Vertx}
     */
    public void clear() {
        Stream.concat(this.httpRegistries.values().stream(), this.wsRegistries.values().stream())
              .parallel()
              .forEach(IClientDelegate::close);
    }

}
