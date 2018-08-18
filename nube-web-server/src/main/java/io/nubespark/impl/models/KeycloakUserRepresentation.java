package io.nubespark.impl.models;

import io.nubespark.Model;
import io.vertx.core.json.JsonObject;

public class KeycloakUserRepresentation extends Model {
    private String username;
    private String firstName;
    private String lastName;
    private String email;

    public KeycloakUserRepresentation(JsonObject body) {
        this.input.put("body", body);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject keyCloakUserRepresentation = super.toJsonObject();
        keyCloakUserRepresentation.put("firstName", this.input.get("body").getString("first_name", ""));
        keyCloakUserRepresentation.put("lastName", this.input.get("body").getString("last_name", ""));
        return keyCloakUserRepresentation;
    }
}
