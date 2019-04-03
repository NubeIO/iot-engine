package com.nubeiot.core.http.rest;

import java.util.List;

import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.RestEventApiMetadata;
import com.nubeiot.core.http.handler.RestEventResultHandler;

/**
 * Make a mapping dynamically between {@code HTTP endpoint} and {@code EventBus}
 */
public interface RestEventApi extends ActionMethodMapping {

    List<RestEventApiMetadata> getRestMetadata();

    @SuppressWarnings("unchecked")
    default <T extends RestEventResultHandler> Class<T> handler() {
        return (Class<T>) RestEventResultHandler.class;
    }

}
