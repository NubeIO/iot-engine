package com.nubeiot.dashboard;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class UserImpl extends Model implements User {

    private String _id;
    private String user_id;
    private String username;
    private String first_name;
    private String last_name;
    private String email;
    private Role role;
    private String address;
    private String phone_no;
    private String associated_company_id;
    private String company_id;
    private String[] sites_ids;
    private String site_id;
    private String group_id;
    private String access_token;

    public UserImpl(JsonObject body) {
        super(body);
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
