package io.nubespark.impl.models;

import io.nubespark.Model;
import io.vertx.core.json.JsonObject;

public class Site extends Model {
    public String _id;
    public String associated_company_id;
    public String site_title;
    public String issue_address;
    public String top_bar_address;
    public String site_footer;
    public String content_background_color;
    public String content_text_color;
    public String content_text_color_header;
    public String selected_text_color_header;
    public String site_text_color;

    public Site(JsonObject body) {
        this.input.put("body", body);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = super.toJsonObject();
        // Combination of 'associated_company_id' and 'site_title' is the primary key for this site
        jsonObject.put("_id", jsonObject.getString("associated_company_id") + jsonObject.getString("site_title"));
        return jsonObject;
    }
}
