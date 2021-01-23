package com.nubeiot.edge.connector.bacnet.service;

import com.nubeiot.core.rpc.notifier.RpcNotifier;
import com.nubeiot.iotdata.IoTEntity;
import com.serotonin.bacnet4j.event.DeviceEventListener;

/**
 * Represents {@code BACnet notifier} that watches the {@code BACnet event} then do notify to {@code external services}
 *
 * @param <P> Type of IoTEntity
 */
public interface BACnetRpcNotifier<P extends IoTEntity>
    extends BACnetRpcProtocol<P>, RpcNotifier<P>, DeviceEventListener {}
