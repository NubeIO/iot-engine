package com.nubeiot.core.http.rest;

import java.util.Collection;

import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.RestEventApiMetadata;
import com.nubeiot.core.http.handler.RestEventApiDispatcher;

/**
 * Make a mapping dynamically between {@code HTTP endpoint} and {@code EventBus}
 */
public interface RestEventApi extends ActionMethodMapping {

    Collection<RestEventApiMetadata> getRestMetadata();

    @SuppressWarnings("unchecked")
    default <T extends RestEventApiDispatcher> Class<T> dispatcher() {
        return (Class<T>) RestEventApiDispatcher.class;
    }

}
