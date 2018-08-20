package io.nubespark.impl.models;

import io.nubespark.Model;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class UserGroup extends Model {
    private String _id;
    private String associated_company_id;
    private String site_id;
    private String name;
    private List<String> access_pages;

    public UserGroup(JsonObject body) {
        this.input.put("body", body);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = super.toJsonObject();
        jsonObject.put("_id", jsonObject.getString("associated_company_id")
                + jsonObject.getString("site_id")
                + jsonObject.getString("name"));
        return jsonObject;
    }
}
