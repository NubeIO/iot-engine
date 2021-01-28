package com.nubeiot.edge.connector.bacnet.internal.request;

import java.util.Objects;

import io.github.zero88.qwe.dto.msg.RequestData;

import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.mixin.deserializer.EncodableDeserializer;
import com.nubeiot.iotdata.property.PointValue;
import com.serotonin.bacnet4j.service.confirmed.WritePropertyRequest;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import lombok.NonNull;

public class WritePointValueRequestFactory implements ConfirmedRequestFactory<WritePropertyRequest, PropertyValue> {

    @Override
    public @NonNull PropertyValue convertData(@NonNull DiscoveryArguments args, @NonNull RequestData requestData) {
        final PointValue pv = Objects.requireNonNull(PointValue.from(requestData.body()), "Point Value is null");
        final PropertyIdentifier pi = PropertyIdentifier.presentValue;
        final Encodable encodable = EncodableDeserializer.parse(args.objectCode(), pi, pv.getValue());
        if (Objects.isNull(encodable)) {
            throw new IllegalArgumentException("Unrecognized value");
        }
        return new PropertyValue(pi, null, encodable, new UnsignedInteger(pv.getPriority()));
    }

    @Override
    public @NonNull WritePropertyRequest factory(@NonNull DiscoveryArguments args, @NonNull PropertyValue pv) {
        return new WritePropertyRequest(args.objectCode(), pv.getPropertyIdentifier(), pv.getPropertyArrayIndex(),
                                        pv.getValue(), pv.getPriority());
    }

}
