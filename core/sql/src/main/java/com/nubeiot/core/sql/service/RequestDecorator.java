package com.nubeiot.core.sql.service;

import com.nubeiot.core.dto.RequestData;

import lombok.NonNull;

public interface RequestDecorator {

    @NonNull
    default RequestData onHandlingNewResource(@NonNull RequestData requestData) {
        return requestData;
    }

    @NonNull
    default RequestData onHandlingManyResource(@NonNull RequestData requestData) {
        return requestData;
    }

    @NonNull
    default RequestData onHandlingOneResource(@NonNull RequestData requestData) {
        return requestData;
    }

}
