package com.nubeiot.edge.connector.bacnet.service.command;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.iot.connector.command.CommanderApis;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.internal.request.WritePointValueRequestFactory;
import com.nubeiot.edge.connector.bacnet.service.AbstractBACnetService;
import com.nubeiot.edge.connector.bacnet.service.BACnetFunctionApis;

import lombok.NonNull;

public final class WritePointValueCommander extends AbstractBACnetService implements BACnetFunctionApis, CommanderApis {

    WritePointValueCommander(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
    }

    @Override
    public @NonNull DiscoveryLevel level() {
        return DiscoveryLevel.OBJECT;
    }

    @Override
    public String function() {
        return "write-pv";
    }

    @Override
    @EventContractor(action = "SEND", returnType = Single.class)
    public Single<JsonObject> send(@NonNull RequestData requestData) {
        final DiscoveryArguments args = createDiscoveryArgs(requestData, level());
        final BACnetDevice device = getLocalDeviceFromCache(args);
        return device.send(EventAction.SEND, args, requestData, new WritePointValueRequestFactory())
                     .map(JsonData::toJson);
    }

}
