package com.nubeiot.core.http.converter;

import java.util.function.Function;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpException;

public class ResponseDataConverter implements Function<Throwable, ResponseData> {

    public static ResponseData convert(Throwable e) {
        return new ResponseDataConverter().apply(e);
    }

    @Override
    public ResponseData apply(Throwable e) {
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            return new ResponseData().setBodyMessage(new JsonObject().put("error", httpException.getMessage()).encode())
                .setStatusCode(httpException.getStatusCode().code());
        } else {
            return new ResponseData().setBodyMessage(e.getMessage())
                .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        }
    }

}
