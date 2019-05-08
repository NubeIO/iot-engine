package com.nubeiot.edge.module.gateway.mock;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.rest.RestApi;

public class ExternalHttpServer extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        final HttpServerRouter httpRouter = new HttpServerRouter().registerApi(ExternalHttpServer.MockAPI.class);
        this.addProvider(new HttpServerProvider(httpRouter));
    }

    public String configFile() { return "httpService.json"; }

    @Path("/test")
    public static class MockAPI implements RestApi {

        @GET
        @Produces(HttpUtils.DEFAULT_CONTENT_TYPE)
        public JsonObject get() {
            return new JsonObject().put("hello", "test");
        }

        @POST
        @Produces(HttpUtils.DEFAULT_CONTENT_TYPE)
        public JsonObject post(@Context RoutingContext ctx) {
            return ctx.getBodyAsJson();
        }

        @GET
        @Path("/error")
        @Produces(HttpUtils.DEFAULT_CONTENT_TYPE)
        public JsonObject error() {
            throw new NubeException("error");
        }

    }

}
