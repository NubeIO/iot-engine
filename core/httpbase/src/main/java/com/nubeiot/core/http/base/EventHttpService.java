package com.nubeiot.core.http.base;

import java.util.Set;

import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.http.base.event.EventMethodDefinition;

public interface EventHttpService extends EventListener {

    /**
     * @return service name
     */
    String api();

    /**
     * @return service address
     */
    default String address() {
        return this.getClass().getName();
    }

    /**
     * @return router mapping between {@code EventAction} and {@code HttpMethod}
     * @see EventMethodDefinition
     */
    Set<EventMethodDefinition> definitions();

}
