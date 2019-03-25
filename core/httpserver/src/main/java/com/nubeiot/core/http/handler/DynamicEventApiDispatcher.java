package com.nubeiot.core.http.handler;

import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.http.handler.DynamicContextDispatcher.AbstractDynamicContextDispatcher;
import com.nubeiot.core.http.rest.DynamicEventRestApi;
import com.nubeiot.core.micro.ServiceDiscoveryController;

public class DynamicEventApiDispatcher<T extends DynamicEventRestApi> extends AbstractDynamicContextDispatcher<T> {

    public DynamicEventApiDispatcher(T api, String gatewayPath, ServiceDiscoveryController dispatcher) {
        super(api, gatewayPath, dispatcher);
    }

    @Override
    public Single<ResponseData> process(HttpMethod httpMethod, String path, RequestData requestData) {
        return getDispatcher().executeEventMessageService(this::filter, path, httpMethod, requestData);
    }

    @Override
    public void handleResponse(RoutingContext context, ResponseData responseData) {
        EventMessage msg = EventMessage.from(responseData.headers());
        if (msg.isError()) {
            handleErrorMessage(context, JsonData.from(responseData.body(), ErrorMessage.class));
            return;
        }
        super.handleResponse(context, responseData);
    }

}
