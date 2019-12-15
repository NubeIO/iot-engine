package com.nubeiot.edge.connector.bacnet.listener;

import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public final class CovListener extends DeviceEventAdapter implements DeviceEventListener {

    @Override
    public void covNotificationReceived(UnsignedInteger subscriberProcessIdentifier,
                                        ObjectIdentifier initiatingDeviceIdentifier,
                                        ObjectIdentifier monitoredObjectIdentifier, UnsignedInteger timeRemaining,
                                        SequenceOf<PropertyValue> listOfValues) {
        super.covNotificationReceived(subscriberProcessIdentifier, initiatingDeviceIdentifier,
                                      monitoredObjectIdentifier, timeRemaining, listOfValues);
    }

}
