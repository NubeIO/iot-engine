package com.nubeiot.dashboard.models;

import com.nubeiot.dashboard.Model;

import io.vertx.core.json.JsonObject;

public class KeycloakUserRepresentation extends Model {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean enabled;

    public KeycloakUserRepresentation(JsonObject body) {
        super(body);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject keyCloakUserRepresentation = super.toJsonObject();
        keyCloakUserRepresentation.put("firstName", this.input.get("body").getString("first_name", ""));
        keyCloakUserRepresentation.put("lastName", this.input.get("body").getString("last_name", ""));
        keyCloakUserRepresentation.put("enabled", true);
        return keyCloakUserRepresentation;
    }
}
