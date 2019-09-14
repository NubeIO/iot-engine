package com.nubeiot.edge.connector.device;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;

public class NetworkVerticle extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        addProvider(new MicroserviceProvider(), this::publishServices);
    }

    @Override
    public void registerEventbus(EventController eventClient) {
        NetworkAppConfig networkAppConfig = IConfig.from(this.nubeConfig.getAppConfig(), NetworkAppConfig.class);
        NetworkCommand networkCommand = JsonData.convert(networkAppConfig.toJson(), NetworkCommand.class);
        eventClient.register(NetworkEventModels.NETWORK_IP, new NetworkIPEventHandler(networkCommand));
    }

    private void publishServices(MicroContext microContext) {
        microContext.getLocalController()
                    .addEventMessageRecord("bios.monitor.network-updater", NetworkEventModels.NETWORK_IP.getAddress(),
                                           EventMethodDefinition.create("/network/ip", networkIpActionMethodMapping()))
                    .subscribe();
    }

    private ActionMethodMapping networkIpActionMethodMapping() {
        Map<EventAction, HttpMethod> map = new HashMap<>();
        map.put(EventAction.UPDATE, HttpMethod.PUT);
        map.put(EventAction.REMOVE, HttpMethod.DELETE);
        return () -> map;
    }

}
