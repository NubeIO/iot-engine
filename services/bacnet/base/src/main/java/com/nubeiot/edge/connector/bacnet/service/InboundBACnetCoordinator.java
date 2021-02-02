package com.nubeiot.edge.connector.bacnet.service;

import io.github.zero88.qwe.iot.connector.coordinator.InboundCoordinator;

import com.nubeiot.edge.connector.bacnet.BACnetProtocol;

/**
 * Represents {@code BACnet notifier} that watches the {@code BACnet event} then do notify to {@code external services}
 */
public interface InboundBACnetCoordinator extends BACnetProtocol, InboundCoordinator {}
