package com.nubeiot.edge.connector.bacnet.handler;

import io.github.zero88.msa.bp.component.ApplicationProbeHandler.ApplicationReadinessHandler;
import io.github.zero88.msa.bp.dto.ErrorData;
import io.github.zero88.msa.bp.dto.msg.RequestData;
import io.github.zero88.msa.bp.event.EventContractor;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;

import lombok.NonNull;

/**
 * Represents for {@code discover completion handler} that listens {@code success} or {@code error} event after scanning
 * network and initializing {@code local BACnet device}.
 *
 * @see BACnetDevice
 * @since 1.0.0
 */
public class DiscoverCompletionHandler implements ApplicationReadinessHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    @EventContractor(action = "NOTIFY", returnType = boolean.class)
    public boolean success(@NonNull RequestData requestData) {
        logger.info(requestData.toJson());
        return true;
    }

    @Override
    @EventContractor(action = "NOTIFY_ERROR", returnType = boolean.class)
    public boolean error(@NonNull ErrorData error) {
        logger.info(error.toJson());
        return true;
    }

}
