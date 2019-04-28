package com.nubeiot.edge.module.gateway;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.rest.provider.RestMicroContextProvider;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.zandero.rest.RestRouter;

public class EdgeGatewayVerticle extends ContainerVerticle {

    private MicroContext microContext;

    @Override
    public void start() {
        super.start();
        HttpServerRouter router = new HttpServerRouter().registerApi(DriverRegistrationApi.class);
        this.addProvider(new HttpServerProvider(router))
            .addProvider(new MicroserviceProvider(), c -> this.microContext = (MicroContext) c);

        this.registerSuccessHandler(event -> RestRouter.addProvider(RestMicroContextProvider.class,
                                                                    ctx -> new RestMicroContextProvider(microContext)));
    }

}
