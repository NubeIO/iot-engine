package com.nubeiot.core.sql.service;

import com.nubeiot.core.dto.RequestData;

import lombok.NonNull;

public interface RequestDecorator {

    @NonNull
    default RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        return requestData;
    }

    @NonNull
    default RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        return requestData;
    }

    @NonNull
    default RequestData onReadingManyResource(@NonNull RequestData requestData) {
        return requestData;
    }

    @NonNull
    default RequestData onReadingOneResource(@NonNull RequestData requestData) {
        return requestData;
    }

}
