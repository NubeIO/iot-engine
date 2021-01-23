package com.nubeiot.edge.installer.service;

import com.nubeiot.core.event.EventListener;

public interface DeploymentService extends EventListener {

    <D> D sharedData(String dataKey);

}
