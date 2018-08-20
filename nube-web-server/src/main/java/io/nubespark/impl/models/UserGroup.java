package io.nubespark.impl.models;

import io.nubespark.Model;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class UserGroup extends Model {
    private String name;
    private String site_setting_id;
    private List<String> access_pages;

    public UserGroup(JsonObject body) {
        this.input.put("body", body);
    }
}
