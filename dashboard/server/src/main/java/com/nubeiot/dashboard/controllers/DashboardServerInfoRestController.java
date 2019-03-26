package com.nubeiot.dashboard.controllers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.nubeiot.core.http.rest.RestApi;
import com.zandero.rest.annotation.RouteOrder;

import io.vertx.core.json.JsonObject;

@Path("/api")
public class DashboardServerInfoRestController implements RestApi {

    @GET
    @Path("/info")
    @RouteOrder(1)
    public JsonObject info() {
        return new JsonObject().put("name", "Dashboard Server Verticle")
                               .put("version", "1.0")
                               .put("vert.x_version", "3.4.1")
                               .put("java_version", "8.0");
    }

}
