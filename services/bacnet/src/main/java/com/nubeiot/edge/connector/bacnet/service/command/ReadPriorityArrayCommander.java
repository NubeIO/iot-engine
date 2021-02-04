package com.nubeiot.edge.connector.bacnet.service.command;

import java.util.Collections;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.iot.connector.command.CommanderApis;
import io.github.zero88.qwe.micro.http.ActionMethodMapping;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.internal.request.ReadPriorityArrayRequestFactory;
import com.nubeiot.edge.connector.bacnet.service.AbstractBACnetService;
import com.nubeiot.edge.connector.bacnet.service.BACnetFunctionApis;

import lombok.NonNull;

public final class ReadPriorityArrayCommander extends AbstractBACnetService
    implements BACnetFunctionApis, CommanderApis {

    protected ReadPriorityArrayCommander(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
    }

    @Override
    public @NonNull String function() {
        return "read/priority-array";
    }

    @Override
    @EventContractor(action = "SEND", returnType = Single.class)
    public Single<JsonObject> send(@NonNull RequestData requestData) {
        final DiscoveryArguments args = createDiscoveryArgs(requestData, level());
        final BACnetDevice device = getLocalDeviceFromCache(args);
        return device.send(EventAction.SEND, args, requestData, new ReadPriorityArrayRequestFactory())
                     .map(JsonData::toJson);
    }

    @Override
    public @NonNull DiscoveryLevel level() {
        return DiscoveryLevel.OBJECT;
    }

    @Override
    public @NonNull ActionMethodMapping eventMethodMap() {
        return ActionMethodMapping.create(Collections.singletonMap(EventAction.SEND, HttpMethod.GET));
    }

}
