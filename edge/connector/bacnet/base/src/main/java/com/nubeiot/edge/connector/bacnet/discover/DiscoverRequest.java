package com.nubeiot.edge.connector.bacnet.discover;

import java.util.Optional;

import io.github.zero.utils.Functions;
import io.github.zero.utils.Strings;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = DiscoverRequest.Builder.class)
@FieldNameConstants(level = AccessLevel.PUBLIC)
public final class DiscoverRequest implements JsonData {

    private final JsonObject network;
    private final String networkCode;
    private final JsonObject device;
    private final Integer deviceCode;
    private final JsonObject object;
    private final String objectCode;

    public static DiscoverRequest from(@NonNull RequestData requestData, @NonNull DiscoverLevel level) {
        final JsonObject body = Optional.ofNullable(requestData.body()).orElse(new JsonObject());
        final String networkCode = DiscoverLevel.NETWORK.isSkipValidate(level)
                                   ? null
                                   : Strings.requireNotBlank(body.getString(Fields.networkCode),
                                                             "Missing BACnet network code");
        final Integer deviceCode = DiscoverLevel.DEVICE.isSkipValidate(level) ? null : Functions.getOrThrow(
                                       () -> Functions.toInt().apply(body.getString(Fields.deviceCode)),
                                       t -> new IllegalArgumentException("Missing BACnet device code", t));
        final String objectCode = DiscoverLevel.OBJECT.isSkipValidate(level)
                                  ? null
                                  : Strings.requireNotBlank(body.getString(Fields.objectCode),
                                                            "Missing BACnet object code");
        return DiscoverRequest.builder()
                              .network(body.getJsonObject(Fields.network, new JsonObject()))
                              .networkCode(networkCode)
                              .device(body.getJsonObject(Fields.device, new JsonObject()))
                              .deviceCode(deviceCode)
                              .object(body.getJsonObject(Fields.object, new JsonObject()))
                              .objectCode(objectCode)
                              .build();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum DiscoverLevel {

        NETWORK(1), DEVICE(2), OBJECT(3);

        private final int level;

        boolean isSkipValidate(@NonNull DiscoverLevel given) {
            return this.level > given.level;
        }
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}

}
