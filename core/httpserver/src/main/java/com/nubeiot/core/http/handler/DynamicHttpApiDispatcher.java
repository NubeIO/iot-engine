package com.nubeiot.core.http.handler;

import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.handler.DynamicContextDispatcher.AbstractDynamicContextDispatcher;
import com.nubeiot.core.http.rest.DynamicHttpRestApi;
import com.nubeiot.core.micro.ServiceDiscoveryController;

public class DynamicHttpApiDispatcher<T extends DynamicHttpRestApi> extends AbstractDynamicContextDispatcher<T> {

    public DynamicHttpApiDispatcher(T api, String gatewayPath, ServiceDiscoveryController dispatcher) {
        super(api, gatewayPath, dispatcher);
    }

    @Override
    public Single<ResponseData> process(HttpMethod httpMethod, String path, RequestData requestData) {
        return getDispatcher().executeHttpService(this::filter, path, httpMethod, requestData);
    }

}
