package com.nubeiot.edge.connector.bacnet.listener;

import java.util.function.Function;

import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.connector.bacnet.service.BACnetNotifier;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import lombok.NonNull;

public final class CovNotifier extends DeviceEventAdapter implements DeviceEventListener, BACnetNotifier<CovNotifier> {

    //TODO implement it
    @Override
    public void covNotificationReceived(UnsignedInteger subscriberProcessIdentifier, ObjectIdentifier initiatingDeviceIdentifier,
                                        ObjectIdentifier monitoredObjectIdentifier, UnsignedInteger timeRemaining,
                                        SequenceOf<PropertyValue> listOfValues) {
        super.covNotificationReceived(subscriberProcessIdentifier, initiatingDeviceIdentifier, monitoredObjectIdentifier, timeRemaining, listOfValues);
    }

    @Override
    public <D> D getSharedDataValue(String dataKey) {
        return null;
    }

    @Override
    public CovNotifier registerSharedData(@NonNull Function<String, Object> sharedDataFunc) {
        return this;
    }

    @Override
    public @NonNull EntityMetadata representation() {
        return PointCompositeMetadata.INSTANCE;
    }

}
