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
import com.nubeiot.edge.connector.device.handlers.DeviceDhcpEventHandler;
import com.nubeiot.edge.connector.device.handlers.DeviceIpEventHandler;
import com.nubeiot.edge.connector.device.handlers.DeviceNetworkEventHandler;
import com.nubeiot.edge.connector.device.handlers.DeviceStatusEventHandler;

public class DeviceVerticle extends ContainerVerticle {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void start() {
        logger.info("Starting DeviceVerticle");
        super.start();
        addProvider(new MicroserviceProvider(), this::publishServices);
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(DeviceEventModels.DEVICE_STATUS, new DeviceStatusEventHandler());
        controller.register(DeviceEventModels.DEVICE_NETWORK, new DeviceNetworkEventHandler());
        controller.register(DeviceEventModels.DEVICE_IP, new DeviceIpEventHandler());
        controller.register(DeviceEventModels.DEVICE_DHCP, new DeviceDhcpEventHandler());
    }

    private void publishServices(MicroContext microContext) {
        microContext.getLocalController()
                    .addEventMessageRecord("device-status", DeviceEventModels.DEVICE_STATUS.getAddress(),
                                           EventMethodDefinition.create("/device/status", getListActionMethodMapping()))
                    .subscribe();

        microContext.getLocalController()
                    .addEventMessageRecord("device-network", DeviceEventModels.DEVICE_NETWORK.getAddress(),
                                           EventMethodDefinition.create("/device/network",
                                                                        getListActionMethodMapping()))
                    .subscribe();

        microContext.getLocalController()
                    .addEventMessageRecord("device-ip", DeviceEventModels.DEVICE_IP.getAddress(),
                                           EventMethodDefinition.create("/device/ip", createActionMethodMapping()))
                    .subscribe();

        microContext.getLocalController()
                    .addEventMessageRecord("device-dhcp", DeviceEventModels.DEVICE_DHCP.getAddress(),
                                           EventMethodDefinition.create("/device/dhcp", createActionMethodMapping()))
                    .subscribe();
    }

    private ActionMethodMapping getListActionMethodMapping() {
        return () -> new HashMap<EventAction, HttpMethod>() {
            {
                put(EventAction.GET_LIST, HttpMethod.GET);
            }
        };
    }

    private ActionMethodMapping createActionMethodMapping() {
        return () -> new HashMap<EventAction, HttpMethod>() {
            {
                put(EventAction.CREATE, HttpMethod.POST);
            }
        };
    }

}
