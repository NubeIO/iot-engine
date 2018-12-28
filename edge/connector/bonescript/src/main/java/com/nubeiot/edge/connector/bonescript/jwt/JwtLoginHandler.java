package com.nubeiot.edge.connector.bonescript.jwt;

import com.nubeiot.core.http.ApiConstants;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class JwtLoginHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext rc) {
        JsonObject body = rc.getBodyAsJson();

        // todo: get username and password from DB
        String username = "";
        String password = "";

        if (body.getString("username", "").equals(username)
            && body.getString("password", "").equals(password)) {
            rc.response().setStatusCode(HttpResponseStatus.OK.code())
              .putHeader(ApiConstants.CONTENT_TYPE, ApiConstants.DEFAULT_CONTENT_TYPE)
              .end(new JsonObject().put("access_token", JwtAccessTokenProvider.create()).encode());
        }
    }

}
