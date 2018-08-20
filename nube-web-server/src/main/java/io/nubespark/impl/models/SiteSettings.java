package io.nubespark.impl.models;

import io.nubespark.Model;
import io.vertx.core.json.JsonObject;

public class SiteSettings extends Model {
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

    public SiteSettings(JsonObject body) {
        this.input.put("body", body);
    }
}
