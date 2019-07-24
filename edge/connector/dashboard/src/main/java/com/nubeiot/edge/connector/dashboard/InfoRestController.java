package com.nubeiot.edge.connector.dashboard;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.http.rest.RestApi;
import com.zandero.rest.annotation.RouteOrder;

public class InfoRestController implements RestApi {

    @GET
    @Path("/info")
    @RouteOrder(1)
    public JsonObject info() {
        return new JsonObject().put("name", "Edge Dashboard Server Verticle")
                               .put("version", "1.0")
                               .put("vert.x_version", "3.4.1")
                               .put("java_version", "8.0");
    }

}
