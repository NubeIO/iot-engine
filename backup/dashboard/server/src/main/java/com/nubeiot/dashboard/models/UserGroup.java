package com.nubeiot.dashboard.models;

import com.nubeiot.dashboard.Model;

import io.vertx.core.json.JsonObject;

public class UserGroup extends Model {
    private String associated_company_id;
    private String site_id;
    private String name;
    private String[] access_pages;

    public UserGroup(JsonObject body) {
        super(body);
    }
}
