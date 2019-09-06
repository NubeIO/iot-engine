package com.nubeiot.dashboard.connector.ditto;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.types.HttpLocation;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.HttpConfig;
import com.nubeiot.core.http.HttpServerContext;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.ServerInfo;
import com.nubeiot.core.http.client.HttpClientConfig;
import com.nubeiot.core.http.client.HttpClientRegistry;
import com.nubeiot.core.http.rest.provider.RestHttpClientConfigProvider;
import com.nubeiot.core.http.rest.provider.RestHttpConfigProvider;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.zandero.rest.RestRouter;

public class ServerDittoDriver extends ContainerVerticle {

    private HttpServerContext httpContext;
    private MicroContext microContext;

    @Override
    @SuppressWarnings("Duplicates")
    public void start() {
        super.start();
        HttpServerRouter router = new HttpServerRouter().registerApi(ServerDittoRestController.class);
        this.addProvider(new HttpServerProvider(router), c -> this.httpContext = (HttpServerContext) c)
            .addProvider(new MicroserviceProvider(), c -> this.microContext = (MicroContext) c);

        this.registerSuccessHandler(event -> {
            ServerInfo info = this.httpContext.getServerInfo();
            microContext.getClusterController()
                        .addHttpRecord("ServerDittoDriver", new HttpLocation(info.toJson()).setRoot(info.getApiPath()),
                                       new JsonObject())
                        .subscribe();
        });

        HttpConfig httpConfig = IConfig.from(this.nubeConfig.getAppConfig(), HttpConfig.class);
        DittoConfig dittoConfig = IConfig.from(this.nubeConfig.getAppConfig(), DittoConfig.class);
        HttpClientConfig httpClientConfig = IConfig.from(this.nubeConfig.getAppConfig(), HttpClientConfig.class);

        RestRouter.addProvider(RestHttpConfigProvider.class, ctx -> new RestHttpConfigProvider(httpConfig));
        RestRouter.addProvider(RestDittoConfigProvider.class, ctx -> new RestDittoConfigProvider(dittoConfig));
        RestRouter.addProvider(RestHttpClientConfigProvider.class,
                               ctx -> new RestHttpClientConfigProvider(httpClientConfig));
    }

    @Override
    public void stop(Future<Void> future) {
        HttpClientRegistry.getInstance().clear();
        super.stop(future);
    }

}
