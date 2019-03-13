package com.nubeiot.core.http.handler;

import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.rest.DynamicHttpRestApi;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.ServiceDiscoveryController;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DynamicContextHttpApiDispatcher<T extends DynamicHttpRestApi> implements DynamicContextDispatcher<T> {

    private final T dynamicHttpRestApi;
    @Getter
    private final MicroContext microContext;
    @Getter
    private final boolean local;

    @Override
    public T get() {
        return dynamicHttpRestApi;
    }

    @Override
    public Single<ResponseData> process(ServiceDiscoveryController dispatcher, HttpMethod httpMethod, String path,
                                        RequestData requestData) {
        //TODO enhance filter record
        return dispatcher.executeHttpService(
            r -> r.getName().equals(get().byName()) && get().byMetadata().equals(r.getMetadata()), path, httpMethod,
            requestData);
    }

}
