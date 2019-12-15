package com.nubeiot.edge.module.gateway;

import io.vertx.servicediscovery.Record;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.eventbus.edge.gateway.GatewayEventBus;

import lombok.Getter;

public final class EdgeGatewayVerticle extends ContainerVerticle {

    @Getter
    private MicroContext microContext;

    @Override
    public void start() {
        super.start();
        this.addProvider(new HttpServerProvider(initHttpRouter()))
            .addProvider(new MicroserviceProvider(), microContext -> this.microContext = (MicroContext) microContext)
            .registerSuccessHandler(event -> successHandler());
    }

    private HttpServerRouter initHttpRouter() {
        return new HttpServerRouter().registerGatewayApi(RouterRegistrationApi.class);
    }

    private void successHandler() {
        final EventbusClient client = getEventbusClient();
        client.register(GatewayEventBus.ROUTER_REGISTRATION,
                        new RouterRegistrationListener(microContext, GatewayEventBus.ROUTER_REGISTRATION.getEvents()))
              .register(GatewayEventBus.ROUTER_ANNOUNCEMENT,
                        new BiosRouterAnnounceHandler(getVertx(), getSharedKey(), microContext,
                                                      GatewayEventBus.ROUTER_ANNOUNCEMENT.getEvents()));
        microContext.getLocalController()
                    .getRecords()
                    .flattenAsObservable(records -> records)
                    .map(Record::toJson)
                    .forEach(r -> client.send(GatewayEventBus.ROUTER_ANNOUNCEMENT.getAddress(),
                                              EventMessage.initial(EventAction.MONITOR,
                                                                   RequestData.builder().body(r).build().toJson())));
    }

}
