package com.nubeiot.core.micro.monitor;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.micro.monitor.ServiceGatewayMonitor.AbstractServiceGatewayMonitor;

import lombok.Getter;

@Getter
public class ServiceGatewayUsageMonitor extends AbstractServiceGatewayMonitor {

    protected ServiceGatewayUsageMonitor(Vertx vertx, ServiceDiscoveryController controller, String sharedKey) {
        super(vertx, controller, sharedKey);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ServiceGatewayUsageMonitor> T create(Vertx vertx, ServiceDiscoveryController controller,
                                                                  String sharedKey, String className) {
        return (T) ServiceGatewayMonitor.create(vertx, controller, sharedKey, className,
                                                ServiceGatewayUsageMonitor.class);
    }

    @Override
    public void handle(Message<Object> message) {
        String msg = "SERVICE USAGE GATEWAY::Receive message from:";
        logger.info("{} '{}'", msg, message.address());
        if (logger.isTraceEnabled()) {
            logger.trace("{} '{}' - Headers: '{}' - Body: '{}'", message.address(), message.headers(), message.body());
        }
    }

}
