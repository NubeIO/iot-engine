package com.nubeiot.edge.connector.bacnet.dto;

import java.util.concurrent.TimeUnit;

import io.vertx.core.shareddata.Shareable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = BACnetDeviceMetadata.Builder.class)
public final class BACnetDeviceMetadata implements JsonData, Shareable {

    private static final long MAX_TIMEOUT_SECONDS = 60;
    private final int vendorId;
    private final String vendorName;
    private final int deviceNumber;
    private final String modelName;
    private final String objectName;
    private final long discoverTimeout;
    private final TimeUnit discoverTimeoutUnit;
    private final boolean slave;

    public static long maxTimeout(long timeout) {
        return Math.min(timeout, MAX_TIMEOUT_SECONDS);
    }

    public LocalDevice decorate(@NonNull LocalDevice localDevice) {
        localDevice.writePropertyInternal(PropertyIdentifier.vendorIdentifier, new UnsignedInteger(vendorId));
        localDevice.writePropertyInternal(PropertyIdentifier.vendorName, new CharacterString(vendorName));
        localDevice.writePropertyInternal(PropertyIdentifier.modelName, new CharacterString(modelName));
        localDevice.writePropertyInternal(PropertyIdentifier.objectType, ObjectType.device);
        localDevice.writePropertyInternal(PropertyIdentifier.objectName, new CharacterString(objectName));
        return localDevice;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        public BACnetDeviceMetadata build() {
            long timeout = TimeUnit.SECONDS.convert(discoverTimeout, discoverTimeoutUnit);
            return new BACnetDeviceMetadata(vendorId, vendorName, deviceNumber, modelName, objectName,
                                            maxTimeout(timeout), TimeUnit.SECONDS, slave);
        }

    }

}
