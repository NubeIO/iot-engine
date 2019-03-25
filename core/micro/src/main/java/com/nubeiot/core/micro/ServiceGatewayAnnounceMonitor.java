package com.nubeiot.core.micro;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.micro.ServiceGatewayMonitor.AbstractServiceGatewayMonitor;

import lombok.Getter;

@Getter
public class ServiceGatewayAnnounceMonitor extends AbstractServiceGatewayMonitor {

    public ServiceGatewayAnnounceMonitor(Vertx vertx, ServiceDiscoveryController controller, String sharedKey) {
        super(vertx, controller, sharedKey);
    }

    @SuppressWarnings("unchecked")
    static <T extends ServiceGatewayAnnounceMonitor> T create(Vertx vertx, ServiceDiscoveryController controller,
                                                              String sharedKey, String className) {
        return (T) ServiceGatewayMonitor.create(vertx, controller, sharedKey, className,
                                                ServiceGatewayAnnounceMonitor.class);
    }

    protected void handle(Record record) { }

    @Override
    public final void handle(Message<Object> message) {
        logger.info("SERVICE ANNOUNCEMENT GATEWAY::Receive message from: \"{}\" - Headers: \"{}\" - Record: \"{}\"",
                    message.address(), message.headers(), message.body());
        handle(new Record((JsonObject) message.body()));
    }

}
