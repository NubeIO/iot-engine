package com.nubeiot.edge.module.gateway;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.http.rest.provider.RestMicroContextProvider;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.edge.module.gateway.handlers.DriverRegistrationEventHandler;
import com.nubeiot.eventbus.edge.gateway.GatewayEventBus;
import com.zandero.rest.RestRouter;

import lombok.Getter;

public class EdgeGatewayVerticle extends ContainerVerticle {

    @Getter
    private MicroContext microContext;

    @Override
    public void start() {
        super.start();
        this.addProvider(new HttpServerProvider(new HttpServerRouter()))
            .addProvider(new MicroserviceProvider(), microContext -> this.microContext = (MicroContext) microContext);

        this.registerSuccessHandler(event -> {
            this.microContext.rescanService(vertx.eventBus().getDelegate());
            RestRouter.addProvider(RestMicroContextProvider.class, ctx -> new RestMicroContextProvider(microContext));
            publishService();
        });
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(GatewayEventBus.DRIVER_REGISTRATION,
                            new DriverRegistrationEventHandler(this, GatewayEventBus.DRIVER_REGISTRATION));
    }

    private void publishService() {
        final ServiceDiscoveryController localController = microContext.getLocalController();
        localController.addEventMessageRecord("drivers_registration", GatewayEventBus.DRIVER_REGISTRATION.getAddress(),
                                              EventMethodDefinition.createDefault("/drivers", "/:registration", false))
                       .subscribe();
    }

}
