package com.nubeiot.edge.connector.bacnet.service;

import io.github.zero88.qwe.iot.connector.coordinator.InboundCoordinator;
import io.github.zero88.qwe.iot.data.IoTEntity;

import com.nubeiot.edge.connector.bacnet.BACnetProtocol;

/**
 * Represents {@code BACnet notifier} that watches the {@code BACnet event} then do notify to {@code external services}
 *
 * @param <P> Type of IoTEntity
 */
public interface InboundBACnetCoordinator<P extends IoTEntity> extends BACnetProtocol, InboundCoordinator<P> {}
