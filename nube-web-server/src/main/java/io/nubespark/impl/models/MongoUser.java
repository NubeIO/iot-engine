package io.nubespark.impl.models;

import io.nubespark.Model;
import io.nubespark.Role;
import io.nubespark.utils.StringUtils;
import io.nubespark.utils.UserUtils;
import io.vertx.core.json.JsonObject;

public class MongoUser extends Model {
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

    public MongoUser(JsonObject body, JsonObject user, JsonObject keycloakUser) {
        super(body);
        this.input.put("user", user);
        this.input.put("keycloakUser", keycloakUser);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject body = this.input.get("body");
        JsonObject user = this.input.get("user");
        JsonObject keycloakUser = this.input.get("keycloakUser");

        JsonObject responseUser = super.toJsonObject();

        responseUser.put("_id", keycloakUser.getString("id"));
        responseUser.put("user_id", keycloakUser.getString("id"));
        responseUser.put("username", keycloakUser.getString("username"));
        if (!keycloakUser.getString("firstName").equals("")) {
            responseUser.put("first_name", keycloakUser.getString("firstName"));
        }
        if (!keycloakUser.getString("lastName").equals("")) {
            responseUser.put("last_name", keycloakUser.getString("lastName"));
        }

        // Role business logic
        Role userRole = Role.GUEST;
        Role setRole = null;
        if (StringUtils.isNotNull(user.getString("role"))) {
            userRole = Role.valueOf(user.getString("role").toUpperCase());
        }
        if (StringUtils.isNotNull(body.getString("role"))) {
            setRole = Role.valueOf(body.getString("role").toUpperCase());
        }
        responseUser.put("role", UserUtils.getRole(userRole, setRole));

        return responseUser;
    }
}
