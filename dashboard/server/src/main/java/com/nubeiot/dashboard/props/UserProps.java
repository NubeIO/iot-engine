package com.nubeiot.dashboard.props;

import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.dashboard.Role;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderClassName = "Builder")
public class UserProps {

    private final MongoClient mongoClient;
    private final HttpClient httpClient;
    private final MicroContext microContext;

    private final String authServerUrl;
    private final String realmName;
    private final String accessToken;
    private final String companyId;
    private final JsonObject user;
    private final Role role;
    private final String defaultPassword = "$password$";

    private JsonObject body;
    private JsonArray arrayBody;
    private String paramsUserId;
    private JsonObject appConfig;
    private RequestData requestData;
    private JsonObject keycloakUser;

    public String getBodyUsername() {
        return this.body.getString("username");
    }

    public String getBodyPassword() {
        return this.body.getString("password", defaultPassword);
    }

    public String getBodyPassword(String defaultPassword) {
        return this.body.getString("password", defaultPassword);
    }

    public Role getBodyRole() {
        return Role.valueOf(this.body.getString("role", ""));
    }

    public String getBodySiteId() {
        return this.body.getString(body.getString("site_id"));
    }

    public String getBodyGroupId() {
        return this.body.getString("group_id", "");
    }

}
