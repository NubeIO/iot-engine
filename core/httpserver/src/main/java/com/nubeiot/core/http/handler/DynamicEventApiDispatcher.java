package com.nubeiot.core.http.handler;

import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.rest.DynamicEventRestApi;
import com.nubeiot.core.micro.ServiceDiscoveryController;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DynamicEventApiDispatcher<T extends DynamicEventRestApi> implements DynamicContextDispatcher<T> {

    private final T dynamicHttpRestApi;
    @Getter
    private final ServiceDiscoveryController dispatcher;

    @Override
    public T get() { return dynamicHttpRestApi; }

    @Override
    public Single<ResponseData> process(HttpMethod httpMethod, String path, RequestData requestData) {
        return dispatcher.executeHttpService(this::filter, path, httpMethod, requestData);
    }

    //TODO enhance filter record
    public boolean filter(Record record) {
        return record.getName().equals(get().name());
    }

}
