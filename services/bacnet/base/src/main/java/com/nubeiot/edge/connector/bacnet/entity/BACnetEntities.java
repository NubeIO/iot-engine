package com.nubeiot.edge.connector.bacnet.entity;

import io.github.zero88.qwe.iot.data.entity.AbstractEntities;

import com.nubeiot.edge.connector.bacnet.mixin.BACnetJsonMixin;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

public interface BACnetEntities {

    final class BACnetNetworks extends AbstractEntities<String, BACnetNetwork> implements BACnetJsonMixin {

    }


    final class BACnetDevices extends AbstractEntities<ObjectIdentifier, BACnetDeviceEntity>
        implements BACnetJsonMixin {

    }


    final class BACnetPoints extends AbstractEntities<ObjectIdentifier, BACnetPointEntity> implements BACnetJsonMixin {

    }

}
