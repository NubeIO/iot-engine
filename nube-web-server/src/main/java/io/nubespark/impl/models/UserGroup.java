package io.nubespark.impl.models;

import io.nubespark.Model;
import io.vertx.core.json.JsonObject;

public class UserGroup extends Model {
    private String associated_company_id;
    private String site_id;
    private String name;
    private String[] access_pages;
    public String role; // ADMIN or MANAGER or USER; defining the company level

    public UserGroup(JsonObject body) {
        super(body);
    }
}
