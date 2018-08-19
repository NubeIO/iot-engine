package io.nubespark.impl;

import io.nubespark.Model;
import io.nubespark.Role;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class UserImpl extends Model implements User {

    private String user_id;
    private Enum<Role> role;
    private String company_id;
    private String group_id; // optional field, only available for the roles GUEST and USER
    private String access_token;

    UserImpl(JsonObject body) {
        this.input.put("body", body);
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
        JsonObject body = super.toJsonObject();
        body.put("role", this.input.get("body").getString("role"));
        return body;
    }

    @Override
    public void setAuthProvider(AuthProvider authProvider) {

    }
}
