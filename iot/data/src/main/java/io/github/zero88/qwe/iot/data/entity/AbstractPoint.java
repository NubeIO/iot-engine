package io.github.zero88.qwe.iot.data.entity;

import io.github.zero88.qwe.iot.data.enums.PointType;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@FieldNameConstants
@Accessors(fluent = true)
public abstract class AbstractPoint<K> implements IPoint<K> {

    @JsonProperty(Fields.key)
    private final K key;
    @NonNull
    @JsonProperty(Fields.networkId)
    private final String networkId;
    @NonNull
    @JsonProperty(Fields.deviceId)
    private final String deviceId;
    @NonNull
    @JsonProperty(Fields.type)
    private final PointType type;

}
