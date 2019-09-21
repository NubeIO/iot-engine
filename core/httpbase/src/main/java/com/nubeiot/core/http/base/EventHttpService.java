package com.nubeiot.core.http.base;

import java.util.Set;

import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.http.base.event.EventMethodDefinition;

public interface EventHttpService extends EventListener {

    String api();

    default String address() {
        return this.getClass().getName();
    }

    Set<EventMethodDefinition> definitions();

}
