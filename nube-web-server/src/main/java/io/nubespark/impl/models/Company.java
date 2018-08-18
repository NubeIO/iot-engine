package io.nubespark.impl.models;

import io.nubespark.Role;
import io.nubespark.Model;
import io.vertx.core.json.JsonObject;

public class Company extends Model {
    private JsonObject requestBody;

    private String name;
    private Role role; // ADMIN or MANAGER; who is responsible for accessing it
    private SiteSettings site_settings;


    public Company(JsonObject body) {
        this.input.put("body", body);
    }

    public JsonObject toJsonObject() {
        JsonObject company = new JsonObject();
        company.put("name", requestBody.getString("name"));
        return company;
    }
}
