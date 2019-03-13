package com.nubeiot.core.micro;

import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ServiceGatewayAnnounceMonitor implements ServiceGatewayMonitor {

    private static final ServiceGatewayAnnounceMonitor DEFAULT = new ServiceGatewayAnnounceMonitor();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("unchecked")
    static <T extends ServiceGatewayAnnounceMonitor> T create(String className) {
        return (T) ServiceGatewayMonitor.create(className, DEFAULT);
    }

    @Override
    public void handle(Message<Object> message) {
        logger.info("SERVICE ANNOUNCEMENT GATEWAY::Receive message from: \"{}\" - Headers: \"{}\" - Body: \"{}\"",
                    message.address(), message.headers(), message.body());
    }

}
