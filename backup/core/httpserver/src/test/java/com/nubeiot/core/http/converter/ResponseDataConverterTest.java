package com.nubeiot.core.http.converter;

import org.junit.Assert;
import org.junit.Test;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.exceptions.NotFoundException;

public class ResponseDataConverterTest {

    @Test
    public void testConvertHttpExceptionToResponseData() {
        int statusCode = HttpResponseStatus.FORBIDDEN.code();
        String message = "You are not Authorized to perform this action";

        HttpException httpException = new HttpException(statusCode, message);
        ResponseData responseData = ResponseDataConverter.convert(httpException);

        Assert.assertEquals(responseData.getStatus().code(), statusCode);
        Assert.assertEquals(responseData.body().getString("message"), new JsonObject().put("error", message).encode());
    }

    @Test
    public void testConvertNotFoundExceptionToResponseData() {
        String message = "Not found";

        NotFoundException notFoundException = new NotFoundException(message);
        ResponseData responseData = ResponseDataConverter.convert(notFoundException);

        Assert.assertEquals(responseData.getStatus().code(), HttpResponseStatus.NOT_FOUND.code());
        Assert.assertEquals(responseData.body().getString("message"), new JsonObject().put("error", message).encode());
    }

    @Test
    public void testConvertThrowableToResponseData() {
        String message = "Something went wrong, internal server error";

        Throwable throwable = new Throwable("Something went wrong, internal server error");
        ResponseData responseData = ResponseDataConverter.convert(throwable);

        Assert.assertEquals(responseData.getStatus().code(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        Assert.assertEquals(responseData.body().getString("message"), new JsonObject().put("error", message).encode());
    }

}
