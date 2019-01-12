package com.nubeiot.edge.connector.bonescript;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.http.rest.RestApi;

import io.vertx.core.json.JsonObject;

@Path(ApiConstants.ROOT_API_PATH)
@Produces(ApiConstants.DEFAULT_CONTENT_TYPE)
public class BoneScriptRestController implements RestApi {

    @GET
    @Path("/info")
    public JsonObject info() {
        return new JsonObject().put("name", "bone-script-connector")
                               .put("version", "1.0.0-SNAPSHOT")
                               .put("vert.x_version", "3.5.4")
                               .put("java_version", "8.0");
    }

}
