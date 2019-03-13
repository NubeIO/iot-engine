package com.nubeiot.core.micro;

import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ServiceGatewayUsageMonitor implements ServiceGatewayMonitor {

    private static final ServiceGatewayUsageMonitor DEFAULT = new ServiceGatewayUsageMonitor();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("unchecked")
    static <T extends ServiceGatewayUsageMonitor> T create(String className) {
        return (T) ServiceGatewayMonitor.create(className, DEFAULT);
    }

    @Override
    public void handle(Message<Object> message) {
        logger.info("SERVICE USAGE GATEWAY::Receive message from: \"{}\" - Headers: \"{}\" - Body: \"{}\"",
                    message.address(), message.headers(), message.body());
    }

}
