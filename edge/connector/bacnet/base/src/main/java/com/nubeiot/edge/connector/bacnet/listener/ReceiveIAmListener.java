package com.nubeiot.edge.connector.bacnet.listener;

import java.util.function.Consumer;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ReceiveIAmListener extends DeviceEventAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoIsListener.class);
    @NonNull
    private final Consumer<RemoteDevice> handler;

    @Override
    public void iAmReceived(RemoteDevice d) {
        handler.andThen(this::log).accept(d);
    }

    private void log(RemoteDevice d) {
        LOGGER.info("Receive IAm from Instance: {} - Address: {}", d.getInstanceNumber(),
                    d.getAddress().getDescription());
    }

}
