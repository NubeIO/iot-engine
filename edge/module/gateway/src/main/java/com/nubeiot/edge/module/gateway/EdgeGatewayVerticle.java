package com.nubeiot.edge.module.gateway;

import java.util.Collections;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.http.rest.provider.RestMicroContextProvider;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.edge.module.gateway.handlers.DriverEventHandler;
import com.nubeiot.edge.module.gateway.handlers.DriverRegistrationEventHandler;
import com.nubeiot.eventbus.edge.gateway.GatewayEventBus;
import com.zandero.rest.RestRouter;

import lombok.Getter;

public class EdgeGatewayVerticle extends ContainerVerticle {

    @Getter
    private MicroContext microContext;
    private EventController controller;

    @Override
    public void start() {
        super.start();
        this.addProvider(new HttpServerProvider(new HttpServerRouter()))
            .addProvider(new MicroserviceProvider(), this::publishService);

        this.registerSuccessHandler(event -> {
            this.microContext.rescanService(vertx.eventBus().getDelegate());
            RestRouter.addProvider(RestMicroContextProvider.class, ctx -> new RestMicroContextProvider(microContext));

            controller.register(GatewayEventBus.DRIVER, new DriverEventHandler(this, GatewayEventBus.DRIVER));
            controller.register(GatewayEventBus.DRIVER_REGISTRATION,
                                new DriverRegistrationEventHandler(this, GatewayEventBus.DRIVER_REGISTRATION));
        });
    }

    @Override
    public void registerEventbus(EventController controller) {
        this.controller = controller;
    }

    private void publishService(MicroContext microContext) {
        this.microContext = microContext;
        final ServiceDiscoveryController localController = microContext.getLocalController();
        localController.addEventMessageRecord("drivers", GatewayEventBus.DRIVER.getAddress(),
                                              EventMethodDefinition.create("/drivers", ActionMethodMapping.create(
                                                  Collections.singletonMap(EventAction.GET_LIST, HttpMethod.GET))))
                       .subscribe();

        localController.addEventMessageRecord("drivers_registration", GatewayEventBus.DRIVER_REGISTRATION.getAddress(),
                                              EventMethodDefinition.createDefault("/drivers/registration",
                                                                                  "/:registration", false)).subscribe();
    }

}
