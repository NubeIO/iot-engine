package io.nubespark.impl.models;

import io.nubespark.Model;
import io.nubespark.utils.SecurityUtils;
import io.vertx.core.json.JsonObject;

public class UserGroup extends Model {
    private String _id;
    private String associated_company_id;
    private String site_id;
    private String name;
    private String[] access_pages;

    public UserGroup(JsonObject body) {
        super(body);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = super.toJsonObject();
        jsonObject.put("_id", SecurityUtils.getBase64EncodedHash(
                jsonObject.getString("associated_company_id")
                        + jsonObject.getString("site_id")
                        + jsonObject.getString("name")));
        return jsonObject;
    }
}
