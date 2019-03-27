package com.nubeiot.core.http.converter;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpException;

import io.netty.handler.codec.http.HttpResponseStatus;

public class ResponseDataConverterTest {

    @Test
    public void testConvertResponseData() {
        int statusCode = HttpResponseStatus.FORBIDDEN.code();
        String message = "You are not Authorized to perform this action";

        HttpException httpException = new HttpException(statusCode, message);
        ResponseData responseData = ResponseDataConverter.convert(httpException);

        Assert.assertEquals(responseData.statusCode(), statusCode);
        Assert.assertEquals(responseData.body().getString("message"), message);
    }

}
