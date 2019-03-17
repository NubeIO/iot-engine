package com.nubeiot.core.http.rest;

import com.nubeiot.core.micro.type.EventMessageService;

public interface DynamicRestEventApi extends DynamicRestApi {

    @Override
    default String type() {
        return EventMessageService.TYPE;
    }

}
