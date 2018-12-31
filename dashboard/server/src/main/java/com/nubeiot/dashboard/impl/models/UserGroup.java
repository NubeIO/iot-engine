package com.nubeiot.dashboard.impl.models;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.common.Model;

public class UserGroup extends Model {
    private String associated_company_id;
    private String site_id;
    private String name;
    private String[] access_pages;

    public UserGroup(JsonObject body) {
        super(body);
    }
}
