package com.nubeiot.edge.core.service;

import com.nubeiot.core.event.EventListener;

public interface DeploymentService extends EventListener {

    <D> D sharedData(String dataKey);

}
