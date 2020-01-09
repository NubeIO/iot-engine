package com.nubeiot.edge.connector.bacnet.handler;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.component.ApplicationProbeHandler.ApplicationReadinessHandler;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.ErrorData;
import com.nubeiot.edge.connector.bacnet.IBACnetDevice;

import lombok.NonNull;

/**
 * Represents for {@code discover completion handler} that listens {@code success} or {@code error} event after scanning
 * network and initializing {@code local BACnet device}.
 *
 * @see IBACnetDevice
 * @since 1.0.0
 */
public class DiscoverCompletionHandler implements ApplicationReadinessHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    @EventContractor(action = EventAction.NOTIFY, returnType = boolean.class)
    public boolean success(@NonNull RequestData requestData) {
        logger.info(requestData.toJson());
        return true;
    }

    @Override
    @EventContractor(action = EventAction.NOTIFY_ERROR, returnType = boolean.class)
    public boolean error(@NonNull ErrorData error) {
        logger.info(error.toJson());
        return true;
    }

}
