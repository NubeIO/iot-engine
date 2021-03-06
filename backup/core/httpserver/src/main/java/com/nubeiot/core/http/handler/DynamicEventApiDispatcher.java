package com.nubeiot.core.http.handler;

import java.util.function.Predicate;

import io.github.zero88.utils.Functions;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.http.converter.RequestDataConverter;
import com.nubeiot.core.http.handler.DynamicContextDispatcher.AbstractDynamicContextDispatcher;
import com.nubeiot.core.http.rest.DynamicEventRestApi;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.micro.type.EventMessageService;

public final class DynamicEventApiDispatcher<T extends DynamicEventRestApi>
    extends AbstractDynamicContextDispatcher<T> {

    DynamicEventApiDispatcher(T api, String gatewayPath, ServiceDiscoveryController dispatcher) {
        super(api, gatewayPath, dispatcher);
    }

    @Override
    public Single<ResponseData> handle(HttpMethod httpMethod, String path, RoutingContext context) {
        if (get().useRequestData()) {
            return getDispatcher().executeEventMessageService(filter(httpMethod, path), path, httpMethod,
                                                              RequestDataConverter.convert(context));
        }
        return getDispatcher().executeEventMessageService(filter(httpMethod, path), path, httpMethod,
                                                          RequestDataConverter.body(context));
    }

    @Override
    public Predicate<Record> filter(HttpMethod method, String path) {
        return Functions.and(super.filter(method, path), record -> EventMethodDefinition.from(
            record.getMetadata().getJsonObject(EventMessageService.EVENT_METHOD_CONFIG)).search(path).isPresent());
    }

    @Override
    public void handleSuccess(RoutingContext context, ResponseData responseData) {
        EventMessage msg = EventMessage.tryParse(responseData.headers());
        if (msg.isError()) {
            handleError(context, JsonData.from(responseData.body(), ErrorMessage.class));
            return;
        }
        super.handleSuccess(context, responseData);
    }

}
