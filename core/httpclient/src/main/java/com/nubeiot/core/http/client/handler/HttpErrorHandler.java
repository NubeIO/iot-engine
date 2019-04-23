package com.nubeiot.core.http.client.handler;

import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import io.netty.resolver.dns.DnsNameResolverException;
import io.netty.resolver.dns.DnsNameResolverTimeoutException;
import io.reactivex.SingleEmitter;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.NubeExceptionConverter;
import com.nubeiot.core.exceptions.TimeoutException;
import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.http.client.HttpClientRegistry;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class HttpErrorHandler implements Handler<Throwable>, Supplier<SingleEmitter<ResponseData>> {

    @NonNull
    private final SingleEmitter<ResponseData> emitter;
    @NonNull
    private final HostInfo hostInfo;

    @SuppressWarnings("unchecked")
    public static <T extends HttpErrorHandler> T create(SingleEmitter<ResponseData> emitter, @NonNull HostInfo hostInfo,
                                                        Class<T> endHandlerClass) {
        if (Objects.isNull(endHandlerClass) || HttpErrorHandler.class.equals(endHandlerClass)) {
            return (T) new HttpErrorHandler(emitter, hostInfo) {};
        }
        Map<Class, Object> inputs = new LinkedHashMap<>();
        inputs.put(SingleEmitter.class, emitter);
        inputs.put(HostInfo.class, hostInfo);
        return (T) ReflectionClass.createObject(endHandlerClass, inputs).get();
    }

    @Override
    public void handle(Throwable error) {
        if (error instanceof VertxException && error.getMessage().equals("Connection was closed") ||
            error instanceof DnsNameResolverTimeoutException) {
            emitter.onError(new TimeoutException("Request timeout", error));
            HttpClientRegistry.getInstance().remove(hostInfo, false);
            return;
        }
        if (error instanceof UnknownHostException || error instanceof DnsNameResolverException) {
            HttpClientRegistry.getInstance().remove(hostInfo, false);
        }
        emitter.onError(NubeExceptionConverter.friendly(error));
    }

    @Override
    public SingleEmitter<ResponseData> get() {
        return emitter;
    }

}
