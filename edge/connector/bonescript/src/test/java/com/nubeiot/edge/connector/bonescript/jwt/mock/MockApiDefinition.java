package com.nubeiot.edge.connector.bonescript.jwt.mock;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.ApiConstants;

import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockApiDefinition {

    @Path("/api")
    public static class MockAPI {

        @GET
        @Produces(ApiConstants.DEFAULT_CONTENT_TYPE)
        public JsonObject get() {
            return new JsonObject().put("abc", "xxx");
        }

        @GET
        @Path("/error")
        @Produces(ApiConstants.DEFAULT_CONTENT_TYPE)
        public JsonObject error() {
            throw new NubeException("error");
        }

        @GET
        @Path("/admin")
        @Produces(ApiConstants.DEFAULT_CONTENT_TYPE)
        @RolesAllowed({"Admin"})
        public JsonObject admin() {
            return new JsonObject().put("role", "Admin");
        }
    }
}
