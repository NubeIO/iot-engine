package io.nubespark.impl;

import io.nubespark.Role;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class UserImpl implements User {

    private String userId;
    private Enum<Role> role;
    private String companyId;
    private String groupId;
    private String access_token;

    UserImpl(String userId, Role role, String companyId, String groupId, String access_token) {
        this.userId = userId;
        this.role = role;
        this.companyId = companyId;
        this.groupId = groupId; // optional field, only available for the roles GUEST and USER
        this.access_token = access_token;
    }

    @Override
    public User isAuthorized(String s, Handler<AsyncResult<Boolean>> handler) {
        return null;
    }

    @Override
    public User clearCache() {
        return null;
    }

    @Override
    public JsonObject principal() {
        return new JsonObject()
                .put("userId", userId)
                .put("role", role)
                .put("companyId", companyId)
                .put("groupId", groupId)
                .put("access_token", access_token);
    }

    @Override
    public void setAuthProvider(AuthProvider authProvider) {

    }
}
