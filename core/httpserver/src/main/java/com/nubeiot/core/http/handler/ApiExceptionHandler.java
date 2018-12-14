package com.nubeiot.core.http.handler;

import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.http.CommonParamParser;
import com.zandero.rest.exception.ExceptionHandler;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public final class ApiExceptionHandler implements ExceptionHandler<Throwable> {

    @Override
    public void write(Throwable result, HttpServerRequest request, HttpServerResponse response) {
        final ErrorMessage parse = ErrorMessage.parse(result);
        response.setStatusCode(HttpStatusMapping.error(request.method(), parse.getThrowable()).code())
                .end(CommonParamParser.prettify(parse, request));
    }

}
