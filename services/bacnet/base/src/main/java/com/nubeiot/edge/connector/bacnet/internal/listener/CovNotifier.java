package com.nubeiot.edge.connector.bacnet.internal.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import lombok.extern.slf4j.Slf4j;

/**
 * @see <a href="https://store.chipkin.com/articles/bacnet-what-is-the-bacnet-change-of-value-cov">COV</a>
 */
@Slf4j
public class CovNotifier extends DeviceEventAdapter implements DeviceEventListener {

    public final List<Map<String, Object>> notifs = new ArrayList<>();

    @Override
    public void covNotificationReceived(final UnsignedInteger subscriberProcessIdentifier,
                                        final ObjectIdentifier initiatingDevice,
                                        final ObjectIdentifier monitoredObjectIdentifier,
                                        final UnsignedInteger timeRemaining,
                                        final SequenceOf<PropertyValue> listOfValues) {
        log.info("COV notification received.");

        final Map<String, Object> notif = new HashMap<>();
        notif.put("subscriberProcessIdentifier", subscriberProcessIdentifier);
        notif.put("initiatingDevice", initiatingDevice);
        notif.put("monitoredObjectIdentifier", monitoredObjectIdentifier);
        notif.put("timeRemaining", timeRemaining);
        notif.put("listOfValues", listOfValues);
        notifs.add(notif);
    }

}
