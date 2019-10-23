package com.nubeiot.edge.connector.bacnet.listener;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.service.Service;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.type.constructed.Address;

public class WhoIsListener extends DeviceEventAdapter implements DeviceEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoIsListener.class);

    @Override
    public void requestReceived(Address from, Service service) {
        LOGGER.info("Address: {} - Global {} | Service: {} - {} - {}", from.toString(), from.isGlobal(),
                    service.getChoiceId(), service.getNetworkPriority(), service.getClass());
        if (service instanceof WhoIsRequest) {
            LOGGER.info("Received WhoIs from {}", from.toString());
        }
    }

}
