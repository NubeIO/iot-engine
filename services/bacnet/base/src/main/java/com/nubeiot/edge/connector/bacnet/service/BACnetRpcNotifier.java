package com.nubeiot.edge.connector.bacnet.service;

import io.github.zero88.qwe.dto.JsonData;

import com.nubeiot.core.rpc.notifier.RpcNotifier;
import com.serotonin.bacnet4j.event.DeviceEventListener;

/**
 * Represents {@code BACnet notifier} that watches the {@code BACnet event} then do notify to {@code Data Point
 * service}
 *
 * @param <P>
 * @param <T>
 */
public interface BACnetRpcNotifier<P extends JsonData, T extends BACnetRpcNotifier>
    extends BACnetRpcProtocol<P>, RpcNotifier<P, T>, DeviceEventListener {}
