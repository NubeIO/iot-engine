package com.nubeiot.edge.connector.bacnet.internal.request;

import io.github.zero88.qwe.dto.msg.RequestData;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.internal.ack.AckServiceHandler;
import com.nubeiot.edge.connector.bacnet.mixin.BACnetJsonMixin;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyAck;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

import lombok.NonNull;

public final class ReadPriorityArrayRequestFactory
    implements ConfirmedRequestFactory<ReadPropertyRequest, ReadPropertyAck, RequestData> {

    @Override
    public @NonNull RequestData convertData(@NonNull DiscoveryArguments args, @NonNull RequestData requestData) {
        return requestData;
    }

    @Override
    public @NonNull ReadPropertyRequest factory(@NonNull DiscoveryArguments args, @NonNull RequestData data) {
        return new ReadPropertyRequest(args.params().objectCode(), PropertyIdentifier.priorityArray);
    }

    @Override
    public AckServiceHandler<ReadPropertyAck> handler() {
        return readPropertyAck -> BACnetJsonMixin.MAPPER.convertValue(readPropertyAck.getValue(), JsonObject.class);
    }

}
