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
import com.nubeiot.core.http.rest.DynamicRestApi;
import com.nubeiot.core.http.utils.RequestDataConverter;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.ServiceDiscoveryController;

public interface DynamicContextDispatcher<T extends DynamicRestApi> extends Handler<RoutingContext>, Supplier<T> {

    MicroContext getMicroContext();

    boolean isLocal();

    Single<ResponseData> process(ServiceDiscoveryController dispatcher, HttpMethod httpMethod, String path,
                                 RequestData requestData);

    boolean filter(Record record);

    @Override
    default void handle(RoutingContext context) {
        HttpMethod httpMethod = validateMethod(context.request().method());
        ServiceDiscoveryController dispatcher = isLocal()
                                                ? getMicroContext().getLocalController()
                                                : getMicroContext().getClusterController();
        RequestData requestData = RequestDataConverter.convert(context);
        String path = context.request().path();
        this.process(dispatcher, httpMethod, path, requestData)
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
