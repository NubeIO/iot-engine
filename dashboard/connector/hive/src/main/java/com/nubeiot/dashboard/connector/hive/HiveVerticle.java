package com.nubeiot.dashboard.connector.hive;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.HttpServerContext;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.RestConfigProvider;
import com.nubeiot.core.http.ServerInfo;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.zandero.rest.RestRouter;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.types.HttpLocation;

public class HiveVerticle extends ContainerVerticle {

    private HttpServerContext httpContext;
    private MicroContext microContext;

    @Override
    @SuppressWarnings("Duplicates")
    public void start() {
        super.start();
        HttpServerRouter router = new HttpServerRouter().registerApi(HiveRestController.class);
        this.addProvider(new HttpServerProvider(router), c -> this.httpContext = (HttpServerContext) c)
            .addProvider(new MicroserviceProvider(), c -> this.microContext = (MicroContext) c);

        this.registerSuccessHandler(event -> {
            ServerInfo info = this.httpContext.getServerInfo();
            microContext.getClusterController()
                        .addHttpRecord("HiveVerticle", new HttpLocation(info.toJson()).setRoot(info.getApiPath()),
                                       new JsonObject())
                        .subscribe();
        });

        RestRouter.addProvider(RestConfigProvider.class, ctx -> new RestConfigProvider(this.nubeConfig));
    }

}
