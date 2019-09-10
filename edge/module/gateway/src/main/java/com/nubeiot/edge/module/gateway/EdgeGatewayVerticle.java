package com.nubeiot.edge.module.gateway;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.rest.provider.RestMicroContextProvider;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.eventbus.edge.gateway.GatewayEventBus;
import com.zandero.rest.RestRouter;

import lombok.Getter;

public class EdgeGatewayVerticle extends ContainerVerticle {

    @Getter
    private MicroContext microContext;

    @Override
    public void start() {
        super.start();
        this.addProvider(new HttpServerProvider(initHttpRouter()))
            .addProvider(new MicroserviceProvider(), microContext -> this.microContext = (MicroContext) microContext);

        this.registerSuccessHandler(event -> {
            this.microContext.rescanService(vertx.eventBus().getDelegate());
            RestRouter.addProvider(RestMicroContextProvider.class, ctx -> new RestMicroContextProvider(microContext));
        });
    }

    private HttpServerRouter initHttpRouter() {
        return new HttpServerRouter().registerGatewayApi(RouteRegistrationApi.class);
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(GatewayEventBus.DRIVER_REGISTRATION,
                            new RouteRegistrationEventListener(this, GatewayEventBus.DRIVER_REGISTRATION));
    }

}
