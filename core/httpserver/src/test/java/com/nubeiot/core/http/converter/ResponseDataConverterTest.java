package com.nubeiot.core.http.converter;

import org.junit.Assert;
import org.junit.Test;

import io.netty.handler.codec.http.HttpResponseStatus;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpException;

public class ResponseDataConverterTest {

    @Test
    public void testConvertHttpExceptionToResponseData() {
        int statusCode = HttpResponseStatus.FORBIDDEN.code();
        String message = "You are not Authorized to perform this action";

        HttpException httpException = new HttpException(statusCode, message);
        ResponseData responseData = ResponseDataConverter.convert(httpException);

        Assert.assertEquals(responseData.statusCode(), statusCode);
        Assert.assertEquals(responseData.body().getString("message"), message);
    }

    @Test
    public void testConvertThrowableToResponseData() {
        String message = "Something went wrong, internal server error";

        Throwable throwable = new Throwable("Something went wrong, internal server error");
        ResponseData responseData = ResponseDataConverter.convert(throwable);

        Assert.assertEquals(responseData.statusCode(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        Assert.assertEquals(responseData.body().getString("message"), message);
    }

}
