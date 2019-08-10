package com.nubeiot.core.http.dynamic.mock;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.types.HttpLocation;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.HttpServerContext;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.ServerInfo;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;

public class MockHttpServiceServer extends ContainerVerticle {

    private HttpServerContext httpContext;
    private MicroContext microContext;

    @Override
    public void start() {
        super.start();
        final HttpServerRouter httpRouter = new HttpServerRouter().registerApi(MockAPI.class);
        this.addProvider(new HttpServerProvider(httpRouter), c -> this.httpContext = (HttpServerContext) c)
            .addProvider(new MicroserviceProvider(), c -> this.microContext = (MicroContext) c);
        this.registerSuccessHandler(event -> {
            ServerInfo info = this.httpContext.getServerInfo();
            microContext.getLocalController()
                        .addHttpRecord("httpService", new HttpLocation(info.toJson()).setRoot(info.getApiPath()),
                                       new JsonObject())
                        .subscribe();
        });
    }

    public String configFile() { return "httpService.json"; }

    @Path("/test")
    public static class MockAPI implements RestApi {

        @GET
        @Produces(HttpUtils.DEFAULT_CONTENT_TYPE)
        public JsonObject get() {
            return new JsonObject().put("hello", "dynamic");
        }

        @GET
        @Path("/error")
        @Produces(HttpUtils.DEFAULT_CONTENT_TYPE)
        public JsonObject error() {
            throw new NubeException("error");
        }

    }

}
