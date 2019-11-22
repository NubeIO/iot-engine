package com.nubeiot.edge.installer.service;

import java.util.Collection;
import java.util.Collections;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.dto.PostDeploymentResult;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class AppDeploymentFinisher implements DeploymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDeploymentFinisher.class);
    private final InstallerEntityHandler entityHandler;

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.NOTIFY);
    }

    @Override
    public <D> D sharedData(String dataKey) {
        return entityHandler.sharedData(dataKey);
    }

    @EventContractor(action = EventAction.NOTIFY, returnType = boolean.class)
    public boolean notify(@Param("result") PostDeploymentResult result) {
        LOGGER.info("INSTALLER::Finish deployment::{}", result.toJson());
        return true;
    }

}
