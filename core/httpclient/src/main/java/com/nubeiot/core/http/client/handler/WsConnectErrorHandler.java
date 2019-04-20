package com.nubeiot.core.http.client.handler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.WebsocketRejectedException;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.TimeoutException;
import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.http.client.HttpClientRegistry;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class WsConnectErrorHandler implements Handler<Throwable> {

    private final HostInfo hostInfo;
    private final EventController controller;

    @SuppressWarnings("unchecked")
    public static <T extends WsConnectErrorHandler> T create(@NonNull HostInfo hostInfo,
                                                             @NonNull EventController controller,
                                                             @NonNull Class<T> connErrorHandlerClass) {
        if (Objects.isNull(connErrorHandlerClass) || WsConnectErrorHandler.class.equals(connErrorHandlerClass)) {
            return (T) new WsConnectErrorHandler(hostInfo, controller) {};
        }
        Map<Class, Object> params = new LinkedHashMap<>();
        params.put(HostInfo.class, hostInfo);
        params.put(EventController.class, controller);
        return ReflectionClass.createObject(connErrorHandlerClass, params);
    }

    @Override
    public void handle(Throwable error) {
        HttpClientRegistry.getInstance().remove(hostInfo, true);
        if (error instanceof VertxException && error.getMessage().equals("Connection was closed")) {
            throw new TimeoutException("Request timeout", error);
        }
        if (error instanceof WebsocketRejectedException) {
            final int status = ((WebsocketRejectedException) error).getStatus();
            throw new HttpException(status, error.getMessage(),
                                    new NubeException(HttpStatusMapping.error(HttpMethod.GET, status), error));
        }
        throw new HttpException("Failed when open websocket connection", error);
    }

}
