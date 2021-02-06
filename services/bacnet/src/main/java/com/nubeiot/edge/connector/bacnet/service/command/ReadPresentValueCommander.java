package com.nubeiot.edge.connector.bacnet.service.command;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.internal.request.ReadPresentValueRequestFactory;
import com.nubeiot.edge.connector.bacnet.service.AbstractBACnetService;

import lombok.NonNull;

public final class ReadPresentValueCommander extends AbstractBACnetService implements BACnetReadCommander {

    protected ReadPresentValueCommander(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
    }

    @Override
    public Single<JsonObject> send(@NonNull RequestData requestData) {
        final DiscoveryArguments args = createDiscoveryArgs(requestData, level());
        final BACnetDevice device = getLocalDeviceFromCache(args);
        return device.send(EventAction.SEND, args, requestData, new ReadPresentValueRequestFactory())
                     .map(JsonData::toJson);
    }

    @Override
    public @NonNull DiscoveryLevel level() {
        return DiscoveryLevel.OBJECT;
    }

    @Override
    public @NonNull String subFunction() {
        return "priority-array";
    }

}
