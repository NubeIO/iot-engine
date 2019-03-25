package com.nubeiot.core.http.handler;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.http.base.HttpUtils;
import com.zandero.rest.exception.ExceptionHandler;

public final class ApiExceptionHandler implements ExceptionHandler<Throwable> {

    @Override
    public void write(Throwable result, HttpServerRequest request, HttpServerResponse response) {
        ErrorMessage errorMessage = ErrorMessage.parse(result);
        response.setStatusCode(HttpStatusMapping.error(request.method(), errorMessage.getThrowable()).code())
                .end(HttpUtils.prettify(errorMessage, request));
    }

}
