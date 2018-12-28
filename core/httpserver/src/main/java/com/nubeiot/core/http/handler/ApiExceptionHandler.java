package com.nubeiot.core.http.handler;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.http.CommonParamParser;
import com.zandero.rest.exception.ExceptionHandler;

public final class ApiExceptionHandler implements ExceptionHandler<Throwable> {

    @Override
    public void write(Throwable result, HttpServerRequest request, HttpServerResponse response) {
        ErrorMessage errorMessage = ErrorMessage.parse(result);
        int statusCode = HttpStatusMapping.error(request.method(), errorMessage.getThrowable()).code();
        String message = CommonParamParser.prettify(errorMessage, request);
        // When other error occurs than the NubeException (e.g.: Raising Authentication Error from base)
        // We need to return meaningful status_code and message
        if (statusCode == HttpResponseStatus.INTERNAL_SERVER_ERROR.code()) {
            statusCode = response.getStatusCode();
            message = result.getMessage();
        }
        response.setStatusCode(statusCode).end(message);
    }

}
