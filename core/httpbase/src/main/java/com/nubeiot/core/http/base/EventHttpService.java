package com.nubeiot.core.http.base;

import java.util.Map;

import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.http.base.event.EventMethodDefinition;

public interface EventHttpService extends EventListener {

    String address();

    Map<String, EventMethodDefinition> definitions();

}
