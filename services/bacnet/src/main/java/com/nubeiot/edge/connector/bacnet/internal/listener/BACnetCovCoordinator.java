package com.nubeiot.edge.connector.bacnet.internal.listener;

import io.github.zero88.qwe.component.SharedDataLocalProxy;

import com.nubeiot.edge.connector.bacnet.service.InboundBACnetCoordinator;
import com.nubeiot.iotdata.IoTEntity;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import lombok.NonNull;

public final class BACnetCovCoordinator extends DeviceEventAdapter
    implements DeviceEventListener, InboundBACnetCoordinator<IoTEntity> {

    @Override
    public void covNotificationReceived(UnsignedInteger subscriberProcessIdentifier,
                                        ObjectIdentifier initiatingDeviceIdentifier,
                                        ObjectIdentifier monitoredObjectIdentifier, UnsignedInteger timeRemaining,
                                        SequenceOf<PropertyValue> listOfValues) {
        super.covNotificationReceived(subscriberProcessIdentifier, initiatingDeviceIdentifier,
                                      monitoredObjectIdentifier, timeRemaining, listOfValues);
    }

    @Override
    public @NonNull Class<IoTEntity> context() {
        return null;
    }

    @Override
    public @NonNull SharedDataLocalProxy sharedData() {
        return null;
    }

    @Override
    public @NonNull String destination() {
        return null;
    }

}
