package com.nubeiot.edge.connector.datapoint.service;

import java.util.Map;

import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.http.base.event.EventMethodDefinition;

public interface EventHttpService extends EventListener {

    String address();

    Map<String, EventMethodDefinition> definitions();

}
