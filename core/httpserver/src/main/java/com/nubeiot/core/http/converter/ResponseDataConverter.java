package com.nubeiot.core.http.converter;

import java.util.function.Function;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpException;

public class ResponseDataConverter implements Function<HttpException, ResponseData> {

    public static ResponseData convert(HttpException e) {
        return new ResponseDataConverter().apply(e);
    }

    @Override
    public ResponseData apply(HttpException e) {
        return new ResponseData().setBodyMessage(e.getMessage()).setStatusCode(e.getStatusCode().code());
    }

}
