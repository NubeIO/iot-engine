package com.nubeiot.core.http.handler;

import java.util.function.Supplier;

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.rest.DynamicEventRestApi;
import com.nubeiot.core.http.rest.DynamicHttpRestApi;
import com.nubeiot.core.http.rest.DynamicRestApi;
import com.nubeiot.core.http.utils.RequestDataConverter;
import com.nubeiot.core.micro.ServiceDiscoveryController;

import lombok.NonNull;

public interface DynamicContextDispatcher<T extends DynamicRestApi> extends Handler<RoutingContext>, Supplier<T> {

    @SuppressWarnings("unchecked")
    static <T extends DynamicRestApi> DynamicContextDispatcher<T> create(@NonNull T api,
                                                                         ServiceDiscoveryController dispatcher) {
        if (api instanceof DynamicHttpRestApi) {
            return new DynamicHttpApiDispatcher((DynamicHttpRestApi) api, dispatcher);
        }
        if (api instanceof DynamicEventRestApi) {
            return new DynamicEventApiDispatcher((DynamicEventRestApi) api, dispatcher);
        }
        return null;
    }

    @NonNull ServiceDiscoveryController getDispatcher();

    Single<ResponseData> process(HttpMethod httpMethod, String path, RequestData requestData);

    boolean filter(Record record);

    @Override
    default void handle(RoutingContext context) {
        HttpMethod httpMethod = validateMethod(context.request().method());
        RequestData requestData = RequestDataConverter.convert(context);
        String path = context.request().path();
        this.process(httpMethod, path.substring(path.indexOf(get().path())), requestData)
            .subscribe(responseData -> handleResponse(context, responseData),
                       throwable -> handleError(context, throwable));
    }

    default void handleResponse(RoutingContext context, ResponseData responseData) {
        context.response()
               .setStatusCode(HttpStatusMapping.success(context.request().method()).code())
               .end(HttpUtils.prettify(responseData.body(), context.request()));
    }

    default void handleError(RoutingContext context, Throwable t) {
        ErrorMessage errorMessage = ErrorMessage.parse(t);
        context.response()
               .setStatusCode(HttpStatusMapping.error(context.request().method(), errorMessage.getCode()).code())
               .end(HttpUtils.prettify(errorMessage.toJson(), context.request()));
    }

    default HttpMethod validateMethod(HttpMethod method) {
        if (get().availableMethods().contains(method)) {
            return method;
        }
        throw new HttpException("Not support HTTP Method " + method);
    }

}
