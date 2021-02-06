package com.nubeiot.edge.connector.bacnet.service.command;

import java.util.Collections;

import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.iot.connector.command.CommanderApis;
import io.github.zero88.qwe.micro.http.ActionMethodMapping;
import io.vertx.core.http.HttpMethod;

import com.nubeiot.edge.connector.bacnet.service.BACnetFunctionApis;

import lombok.NonNull;

public interface BACnetReadCommander extends BACnetFunctionApis, CommanderApis {

    @Override
    default @NonNull String function() {
        return "read/" + subFunction();
    }

    @Override
    default @NonNull ActionMethodMapping eventMethodMap() {
        return ActionMethodMapping.create(Collections.singletonMap(EventAction.SEND, HttpMethod.GET));
    }

    @NonNull String subFunction();

}
