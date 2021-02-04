package com.nubeiot.edge.connector.bacnet.service.command;

import java.util.Collections;
import java.util.Optional;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.event.EventMessage;
import io.github.zero88.qwe.iot.connector.command.CommanderApis;
import io.github.zero88.qwe.micro.http.ActionMethodMapping;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.dto.CovOutput;
import com.nubeiot.edge.connector.bacnet.internal.request.ConfirmedRequestFactory;
import com.nubeiot.edge.connector.bacnet.internal.request.ReadPriorityArrayRequestFactory;
import com.nubeiot.edge.connector.bacnet.service.AbstractBACnetService;
import com.nubeiot.edge.connector.bacnet.service.BACnetFunctionApis;

import lombok.NonNull;

public final class ReadPriorityArrayCommander extends AbstractBACnetService
    implements BACnetFunctionApis, CommanderApis {

    public static final String AS_COV = "asCOV";

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
        final boolean asCov = requestData.filter().getBoolean(AS_COV, false);
        requestData.filter().put(ConfirmedRequestFactory.STRICT, !asCov);
        final BACnetDevice device = getLocalDeviceFromCache(args);
        return device.send(EventAction.SEND, args, requestData, new ReadPriorityArrayRequestFactory())
                     .map(msg -> convert(msg, args, asCov));
    }

    private JsonObject convert(@NonNull EventMessage msg, @NonNull DiscoveryArguments args, boolean asCov) {
        if (!asCov) {
            return Optional.ofNullable(msg.getData()).orElseGet(JsonObject::new);
        }
        return CovOutput.builder()
                        .key(args.key())
                        .cov(msg.getData())
                        .any(msg.isError() ? msg.getError().toJson() : null)
                        .build()
                        .toJson();
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
