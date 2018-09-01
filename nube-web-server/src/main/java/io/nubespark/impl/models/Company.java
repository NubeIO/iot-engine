package io.nubespark.impl.models;

import io.nubespark.Model;
import io.nubespark.Role;
import io.nubespark.utils.UserUtils;
import io.vertx.core.json.JsonObject;

public class Company extends Model {
    public String name;
    public Role role; // ADMIN or MANAGER; defining the company level
    public String associated_company_id; // for pointing parent company; we will point this on company creation

    public Company(JsonObject body, JsonObject user) {
        super(body);
        this.input.put("user", user);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject user = this.input.get("user");
        role = UserUtils.getRole(Role.valueOf(user.getString("role")), null);
        associated_company_id = user.getString("company_id");

        return super.toJsonObject()
                .put("role", role)
                .put("associated_company_id", associated_company_id);
    }
}
