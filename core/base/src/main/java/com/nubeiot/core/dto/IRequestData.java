package com.nubeiot.core.dto;

public interface IRequestData extends JsonData {

    default RequestData toRequestData() {
        return RequestData.builder().body(toJson()).build();
    }

}
