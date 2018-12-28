package com.nubeiot.edge.connector.bonescript.jwt;

import java.util.function.Function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PACKAGE)
public class JwtUser implements User {

    private JwtUserPrincipal principal;

    @Override
    public User isAuthorized(String authority, Handler<AsyncResult<Boolean>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(principal.getRole() != null && principal.getRole().equals(Role.valueOf(authority))));
        return this;
    }

    public void setAuthority(JwtUserPrincipal principal, Handler<AsyncResult<Boolean>> isAuthorized) {
        Function<JwtUserPrincipal, JwtUserPrincipal> tokenAuthentication = new JwtTokenAuthentication<>();
        this.principal = tokenAuthentication.apply(principal);

        if (this.principal.getAuthorized()) {
            isAuthorized.handle(Future.succeededFuture(true));
        } else {
            isAuthorized.handle(Future.succeededFuture(false));
        }
    }

    @Override
    public User clearCache() {
        return this;
    }

    @Override
    public JsonObject principal() {
        return principal.toJson();
    }

    @Override
    public void setAuthProvider(AuthProvider authProvider) {

    }
}
