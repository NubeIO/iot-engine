package com.nubeiot.core.http.converter;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.handler.ResponseDataWriter;

public class ResponseDataConverter {

    public static ResponseData convert(HttpMethod httpMethod, Throwable e) {
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            return ResponseDataWriter.serializeResponseData(
                new JsonObject().put("error", httpException.getMessage()).encode()).setStatus(
                httpException.getStatusCode().code());
        } else if (e instanceof NubeException) {
            NubeException nubeException = (NubeException) e;
            HttpResponseStatus responseStatus = HttpStatusMapping.error(httpMethod, nubeException.getErrorCode());
            return ResponseDataWriter.serializeResponseData(
                new JsonObject().put("error", nubeException.getMessage()).encode()).setStatus(
                responseStatus.code());
        } else {
            return ResponseDataWriter.serializeResponseData(new JsonObject().put("error", e.getMessage()).encode())
                                     .setStatus(
                HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static ResponseData convert(Throwable e) {
        return convert(HttpMethod.GET, e);
    }

}
