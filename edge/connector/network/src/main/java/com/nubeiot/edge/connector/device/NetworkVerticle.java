package com.nubeiot.edge.connector.device;

import java.util.HashMap;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;

public class NetworkVerticle extends ContainerVerticle {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void start() {
        logger.info("Starting Network App Verticle");
        super.start();
        addProvider(new MicroserviceProvider(), this::publishServices);
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(NetworkEventModels.NETWORK_IP, new NetworkIPEventHandler());
    }

    private void publishServices(MicroContext microContext) {
        microContext.getLocalController()
                    .addEventMessageRecord("network-ip", NetworkEventModels.NETWORK_IP.getAddress(),
                                           EventMethodDefinition.create("/network/ip", networkIpActionMethodMapping()))
                    .subscribe();
    }

    private ActionMethodMapping networkIpActionMethodMapping() {
        return () -> new HashMap<EventAction, HttpMethod>() {
            {
                put(EventAction.CREATE, HttpMethod.POST);
                put(EventAction.REMOVE, HttpMethod.DELETE);
            }
        };
    }

}
