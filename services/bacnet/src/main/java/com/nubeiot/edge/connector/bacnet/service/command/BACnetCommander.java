package com.nubeiot.edge.connector.bacnet.service.command;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.iot.connector.command.CommanderApis;
import io.github.zero88.utils.Urls;

import com.nubeiot.edge.connector.bacnet.service.AbstractBACnetService;
import com.nubeiot.edge.connector.bacnet.service.BACnetApis;

import lombok.NonNull;

public abstract class BACnetCommander extends AbstractBACnetService implements CommanderApis, BACnetApis {

    protected BACnetCommander(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
    }

    @Override
    public @NonNull String servicePath() {
        return Urls.combinePath(BACnetApis.super.servicePath(), BACnetApis.super.paramPath());
    }

    @Override
    public String paramPath() {
        return CommanderApis.super.paramPath();
    }

}
