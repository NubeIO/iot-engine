package com.nubeiot.edge.connector.bacnet.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.edge.module.datapoint.rpc.subscriber.DataProtocolSubscriber;
import com.nubeiot.iotdata.dto.Protocol;

/**
 * Represents {@code BACnet subscriber} that listens an appropriate event from {@code NubeIO service} then do dispatch
 * to {@code BACnet Device}
 *
 * @see VertxPojo
 * @see DataProtocolSubscriber
 */
public interface BACnetSubscriber<P extends VertxPojo> extends DataProtocolSubscriber<P> {

    @Override
    default Protocol protocol() {
        return Protocol.BACNET;
    }

}
