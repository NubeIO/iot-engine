package com.nubeiot.edge.connector.bacnet.entity;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.iotdata.entity.AbstractPoint;
import com.nubeiot.iotdata.enums.PointType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents for BACnet point.
 *
 * @see <a href="http://www.bacnet.org/Bibliography/ES-7-96/ES-7-96.htm">BACnet-Objects, Properties and Services</a>
 * @see <a href="https://www.controlscourse.com/bacnet-objects/">BACnet objects</a>
 * @since 1.0.0
 */
@Getter
@Jacksonized
@SuperBuilder
@Accessors(fluent = true)
public class BACnetPointEntity extends AbstractPoint<String> implements BACnetEntity<String> {

    @NonNull
    @JsonIgnore
    private final PropertyValuesMixin mixin;

    public static BACnetPointEntity from(@NonNull String networkId, @NonNull ObjectIdentifier deviceId,
                                         @NonNull PropertyValuesMixin mixin) {
        return from(networkId, ObjectIdentifierMixin.serialize(deviceId), mixin);
    }

    public static BACnetPointEntity from(@NonNull String networkId, @NonNull String deviceId,
                                         @NonNull PropertyValuesMixin mixin) {
        return BACnetPointEntity.builder()
                                .key(ObjectIdentifierMixin.serialize(mixin.getObjectId()))
                                .type(PointType.factory(mixin.encode(PropertyIdentifier.objectType)))
                                .networkId(networkId)
                                .deviceId(deviceId)
                                .mixin(mixin)
                                .build();
    }

    @Override
    public JsonObject toJson(@NonNull ObjectMapper mapper) {
        final JsonObject json = super.toJson(mapper);
        return json.mergeIn(mixin.toJson());
    }

}
