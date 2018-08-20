package io.nubespark.impl.models;

import io.nubespark.Role;
import io.nubespark.Model;
import io.nubespark.utils.UserUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Company extends Model {
    public String _id;
    public String name;
    public Role role; // ADMIN or MANAGER; defining the company level
    public String associated_company_id; // for pointing parent company; we will point this on company creation
    public JsonArray child_company_list_id; // for pointing child companies; we will point this on company creation


    public Company(JsonObject body, JsonObject user) {
        this.input.put("body", body);
        this.input.put("user", user);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject body = this.input.get("body");
        JsonObject user = this.input.get("user");
        _id = body.getString("name");
        role = UserUtils.getRole(Role.valueOf(user.getString("role")), null);
        associated_company_id = user.getString("company_id");
        child_company_list_id = new JsonArray();

        return super.toJsonObject()
                .put("_id", _id)
                .put("role", role)
                .put("associated_company_id", associated_company_id)
                .put("child_company_list_id", child_company_list_id);
    }
}
