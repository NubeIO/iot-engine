package com.nubeiot.core.http.handler;

import java.util.function.Supplier;

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.http.CommonParamParser;
import com.nubeiot.core.http.rest.DynamicRestApi;
import com.nubeiot.core.http.utils.RequestConverter;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.ServiceDiscoveryController;

public interface DynamicContextDispatcher<T extends DynamicRestApi> extends Handler<RoutingContext>, Supplier<T> {

    MicroContext getMicroContext();

    boolean isLocal();

    Single<ResponseData> process(ServiceDiscoveryController dispatcher, HttpMethod httpMethod, String path,
                                 RequestData requestData);

    @Override
    default void handle(RoutingContext context) {
        ServiceDiscoveryController dispatcher = isLocal()
                                                ? getMicroContext().getLocalController()
                                                : getMicroContext().getClusterController();
        RequestData requestData = RequestConverter.convert(context);
        HttpMethod httpMethod = context.request().method();
        String path = context.request().path();
        this.process(dispatcher, httpMethod, path, requestData)
            .subscribe(responseData -> handleResponse(context, responseData),
                       throwable -> handleError(context, throwable));
    }

    default void handleResponse(RoutingContext context, ResponseData responseData) {
        context.response()
               .setStatusCode(HttpStatusMapping.success(context.request().method()).code())
               .end(CommonParamParser.prettify(responseData.body(), context.request()));
    }

    default void handleError(RoutingContext context, Throwable t) {
        ErrorMessage errorMessage = ErrorMessage.parse(t);
        context.response()
               .setStatusCode(HttpStatusMapping.error(context.request().method(), errorMessage.getCode()).code())
               .end(CommonParamParser.prettify(errorMessage.toJson(), context.request()));
    }

}
