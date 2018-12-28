package com.nubeiot.edge.connector.bonescript;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import com.nubeiot.core.http.ApiConstants;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.http.HttpServerRequest;

@Path(ApiConstants.ROOT_API_PATH)
@Produces(ApiConstants.DEFAULT_CONTENT_TYPE)
public class BoneScriptRestController {

    @GET
    @Path("/info")
    public JsonObject info(@Context HttpServerRequest request) {
        return new JsonObject().put("name", "bone-script-connector")
                               .put("version", "1.0.0-SNAPSHOT")
                               .put("vert.x_version", "3.5.4")
                               .put("java_version", "8.0");
    }

}
