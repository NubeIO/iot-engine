package com.nubeiot.edge.connector.bacnet.service.notifier;

import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public final class CovNotifier extends DeviceEventAdapter
    implements DeviceEventListener/*, BACnetRpcNotifier<IPoint, CovNotifier>*/ {

    @Override
    public void covNotificationReceived(UnsignedInteger subscriberProcessIdentifier,
                                        ObjectIdentifier initiatingDeviceIdentifier,
                                        ObjectIdentifier monitoredObjectIdentifier, UnsignedInteger timeRemaining,
                                        SequenceOf<PropertyValue> listOfValues) {
        super.covNotificationReceived(subscriberProcessIdentifier, initiatingDeviceIdentifier,
                                      monitoredObjectIdentifier, timeRemaining, listOfValues);
    }

    //    @Override
    //    public <D> D getSharedDataValue(String dataKey) {
    //        return null;
    //    }
    //
    //    @Override
    //    public CovNotifier registerSharedData(@NonNull Function<String, Object> sharedDataFunc) {
    //        return this;
    //    }

    //    @Override
    //    public @NonNull EntityMetadata context() {
    //        return PointCompositeMetadata.INSTANCE;
    //    }
}
