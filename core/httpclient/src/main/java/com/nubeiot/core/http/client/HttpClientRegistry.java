package com.nubeiot.core.http.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.nubeiot.core.http.base.HostInfo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClientRegistry {

    private static HttpClientRegistry instance;
    private final Map<HostInfo, HttpClientDelegate> httpRegistries = new HashMap<>();
    private final Map<HostInfo, WebsocketClientDelegate> wsRegistries = new HashMap<>();

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

    public void removeWebsocket(@NonNull HostInfo hostInfo) {
        this.wsRegistries.remove(hostInfo);
    }

    public void removeHttpClient(@NonNull HostInfo hostInfo) {
        this.httpRegistries.remove(hostInfo);
    }

}
