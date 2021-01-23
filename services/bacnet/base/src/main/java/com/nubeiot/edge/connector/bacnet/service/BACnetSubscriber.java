package com.nubeiot.edge.connector.bacnet.service;

import io.github.zero88.qwe.dto.JsonData;

import com.nubeiot.core.rpc.subscriber.RpcSubscriber;
import com.nubeiot.edge.connector.bacnet.entity.BACnetEntity;
import com.nubeiot.iotdata.Protocol;

/**
 * Represents {@code BACnet subscriber} that listens an appropriate event from {@code external service} then do dispatch
 * to {@code BACnet Object}
 *
 * @see JsonData
 * @see RpcSubscriber
 */
public interface BACnetSubscriber<P extends BACnetEntity> extends RpcSubscriber<P> {

    @Override
    default Protocol protocol() {
        return Protocol.BACNET;
    }

}
