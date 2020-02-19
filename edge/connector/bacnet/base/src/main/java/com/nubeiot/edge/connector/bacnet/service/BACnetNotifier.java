package com.nubeiot.edge.connector.bacnet.service;

import com.nubeiot.edge.module.datapoint.rpc.notifier.DataProtocolNotifier;
import com.serotonin.bacnet4j.event.DeviceEventListener;

/**
 * Represents {@code BACnet notifier} that watches the {@code BACnet event} then do notify to {@code Data Point
 * service}
 */
public interface BACnetNotifier<T extends BACnetNotifier>
    extends BACnetRpcProtocol, DataProtocolNotifier<T>, DeviceEventListener {}
