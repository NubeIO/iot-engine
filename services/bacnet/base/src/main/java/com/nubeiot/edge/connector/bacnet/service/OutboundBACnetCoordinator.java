package com.nubeiot.edge.connector.bacnet.service;

import io.github.zero88.qwe.dto.JsonData;

import com.nubeiot.core.rpc.coordinator.OutboundCoordinator;
import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.entity.BACnetEntity;

/**
 * Represents {@code BACnet subscriber} that listens an appropriate event from {@code external service} then do dispatch
 * to {@code BACnet Object}
 *
 * @see JsonData
 * @see OutboundCoordinator
 */
public interface OutboundBACnetCoordinator<P extends BACnetEntity> extends OutboundCoordinator<P>, BACnetProtocol {

}
