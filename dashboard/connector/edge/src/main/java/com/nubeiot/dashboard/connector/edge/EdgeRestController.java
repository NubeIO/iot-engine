package com.nubeiot.dashboard.connector.edge;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.http.rest.RestApi;

@Produces(ApiConstants.DEFAULT_CONTENT_TYPE)
public final class EdgeRestController implements RestApi {

    @GET
    @Path("/info")
    public JsonObject info() {
        return new JsonObject().put("name", "edge-connector-rest")
                               .put("version", "1.0.0-SNAPSHOT")
                               .put("vert.x_version", "3.5.4")
                               .put("java_version", "8.0");
    }

    @GET
    @Path("/test")
    public JsonObject test() {
        throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Test exception");
    }

}
