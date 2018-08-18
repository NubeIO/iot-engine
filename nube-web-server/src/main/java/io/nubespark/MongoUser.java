package io.nubespark;

import io.nubespark.utils.UserUtils;
import io.vertx.core.json.JsonObject;

public class MongoUser {
    private JsonObject requestBody;
    private JsonObject user;
    private JsonObject keycloakBody;

    public MongoUser(JsonObject requestBody, JsonObject user, JsonObject keycloakUser) {
        this.requestBody = requestBody;
        this.user = user;
        this.keycloakBody = keycloakUser;
    }

    public JsonObject toJson() {
        JsonObject responseUser = new JsonObject();
        responseUser.put("userId", keycloakBody.getString("id"));
        responseUser.put("username", keycloakBody.getString("username"));
        if (!keycloakBody.getString("firstName").equals("")) {
            responseUser.put("firstName", keycloakBody.getString("firstName"));
        }
        if (!keycloakBody.getString("lastName").equals("")) {
            responseUser.put("lastName", keycloakBody.getString("lastName"));
        }
        if (requestBody.getString("email") !=null) {
            responseUser.put("email", requestBody.getString("email"));
        }

        // Role business logic
        Role userRole = Role.GUEST;
        Role setRole = null;
        if (user.getString("role") != null) {
            userRole = Role.valueOf(user.getString("role").toUpperCase());
        }
        if (requestBody.getString("role") != null) {
            setRole = Role.valueOf(requestBody.getString("role").toUpperCase());
        }
        responseUser.put("role", UserUtils.getRole(userRole, setRole));


        if (requestBody.getString("address") != null) {
            responseUser.put("address", requestBody.getString("address"));
        }
        if (requestBody.getString("phoneNo") != null) {
            responseUser.put("phoneNo", requestBody.getString("phoneNo"));
        }
        
        if (requestBody.getString("company") != null) {
            //todo:company
            responseUser.put("company", requestBody.getString("company"));
        }
        if (requestBody.getString("group") != null) {
            //todo:group
            responseUser.put("group", requestBody.getString("group"));
        }

        return responseUser;
    }
}
