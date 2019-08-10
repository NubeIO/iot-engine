package com.nubeiot.core.http.base;

import java.util.Set;

import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.http.base.event.EventMethodDefinition;

public interface EventHttpService extends EventListener {

    String api();

    String address();

    Set<EventMethodDefinition> definitions();

}
