package com.nubeiot.edge.connector.bonescript.jwt;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.utils.Strings;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class JwtHandler implements Handler<RoutingContext> {

    static final String ACCESS_TOKEN_HEADER_KEY = "access_token";

    @Override
    public void handle(RoutingContext rc) {

        String accessToken = rc.request().getHeader(ACCESS_TOKEN_HEADER_KEY);
        if (Strings.isBlank(accessToken)) {
            accessToken = "";
        }

        JsonObject data = new JsonObject().put("accessToken", accessToken);
        JwtUserPrincipal jwtUserPrincipal = JsonData.from(data, JwtUserPrincipal.class);

        new JwtAuthProvider().authenticate(jwtUserPrincipal.toJson(), user -> {
            if (user.succeeded()) {
                rc.setUser(user.result());
                rc.next();
            } else {
                NubeException exception = (NubeException) user.cause();
                HttpResponseStatus httpResponseStatus = HttpStatusMapping.error(rc.request().method(),
                                                                                exception.getErrorCode());
                rc.response()
                  .putHeader(ApiConstants.CONTENT_TYPE, ApiConstants.DEFAULT_CONTENT_TYPE)
                  .setStatusCode(httpResponseStatus.code())
                  .end(new JsonObject().put("message", httpResponseStatus.reasonPhrase()).encode());
            }
        });
    }

}
