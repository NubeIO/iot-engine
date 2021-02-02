package com.nubeiot.iotdata.entity;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.iotdata.enums.DeviceStatus;
import com.nubeiot.iotdata.enums.DeviceType;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@FieldNameConstants
@Accessors(fluent = true)
public abstract class AbstractDevice<K> implements IDevice<K> {

    @JsonProperty(Fields.key)
    private final K key;
    @NonNull
    @JsonProperty(Fields.networkId)
    private final String networkId;
    @NonNull
    @JsonProperty(Fields.address)
    private final JsonObject address;
    @NonNull
    @JsonProperty(Fields.type)
    private final DeviceType type;
    @NonNull
    @JsonProperty(Fields.name)
    private final String name;
    @NonNull
    @JsonProperty(Fields.status)
    private final DeviceStatus status;

}
