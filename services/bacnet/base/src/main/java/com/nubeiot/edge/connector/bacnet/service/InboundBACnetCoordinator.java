package com.nubeiot.edge.connector.bacnet.service;

import com.nubeiot.core.rpc.coordinator.InboundCoordinator;
import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.iotdata.IoTEntity;
import com.serotonin.bacnet4j.event.DeviceEventListener;

/**
 * Represents {@code BACnet notifier} that watches the {@code BACnet event} then do notify to {@code external services}
 *
 * @param <P> Type of IoTEntity
 */
public interface InboundBACnetCoordinator<P extends IoTEntity>
    extends BACnetProtocol, InboundCoordinator<P>, DeviceEventListener {}
