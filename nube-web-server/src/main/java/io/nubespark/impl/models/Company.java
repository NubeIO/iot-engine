package io.nubespark.impl.models;

import io.nubespark.Role;
import io.nubespark.Model;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class Company extends Model {
    private JsonObject requestBody;

    private String name;
    private Role role; // ADMIN or MANAGER; who is responsible for accessing it
    private String associated_company_id; // for pointing parent company; we will point this on company creation
    private List<String> child_company_id; // for pointing child companies; we will point this on company creation


    public Company(JsonObject body) {
        this.input.put("body", body);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject company = super.toJsonObject();
        return company;
    }
}
