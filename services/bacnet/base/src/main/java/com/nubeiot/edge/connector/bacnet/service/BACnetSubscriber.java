package com.nubeiot.edge.connector.bacnet.service;

import io.github.zero88.qwe.dto.JsonData;

import com.nubeiot.core.rpc.subscriber.RpcSubscriber;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.iotdata.dto.Protocol;

/**
 * Represents {@code BACnet subscriber} that listens an appropriate event from {@code NubeIO service} then do dispatch
 * to {@code BACnet Device}
 *
 * @see JsonData
 * @see RpcSubscriber
 */
public interface BACnetSubscriber<P extends JsonData> extends RpcSubscriber<P> {

    @Override
    default Protocol protocol() {
        return BACnetDevice.BACNET;
    }

}
