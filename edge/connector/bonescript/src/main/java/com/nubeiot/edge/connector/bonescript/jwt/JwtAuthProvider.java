package com.nubeiot.edge.connector.bonescript.jwt;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.SecurityException;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class JwtAuthProvider implements AuthProvider {

    @Override
    public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {

        JwtUser user = new JwtUser();

        user.setAuthority(JsonData.from(authInfo, JwtUserPrincipal.class), isAuthorized -> {
            if (isAuthorized.succeeded()) {
                if (isAuthorized.result()) {
                    resultHandler.handle(Future.succeededFuture(user));
                } else {
                    resultHandler.handle(Future.failedFuture(new SecurityException.AuthenticationException("Authentication Failure!")));
                }
            } else {
                resultHandler.handle(Future.failedFuture(new NubeException(NubeException.ErrorCode.UNKNOWN_ERROR, "Unknown Error!")));
            }
        });

    }
}
