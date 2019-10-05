package com.nubeiot.edge.core.service;

import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventModel;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
final class DefaultDeployerDefinition implements DeployerDefinition {

    @NonNull
    private final EventModel event;
    @NonNull
    private final EventModel postEvent;
    @NonNull
    private final EventListener handler;
    @NonNull
    private final EventListener postHandler;

}
