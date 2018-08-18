package io.nubespark;

import io.vertx.core.json.JsonObject;

public class KeycloakUserRepresentation {
    private String username;
    private String firstName;
    private String lastName;
    private String email;

    public KeycloakUserRepresentation(JsonObject body) {
        this.username = body.getString("username");
        this.firstName = body.getString("firstName", "");
        this.lastName = body.getString("lastName", "");
        this.email = body.getString("email", "");
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("username", username)
                .put("firstName", firstName)
                .put("lastName", lastName)
                .put("email", email);
    }
}
