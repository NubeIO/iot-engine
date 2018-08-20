package io.nubespark.impl.models;

import io.nubespark.Model;
import io.vertx.core.json.JsonObject;

public class SiteSettings extends Model {
    private String associated_company_id;
    private String site_title;
    private String issue_address;
    private String top_bar_address;
    private String site_footer;
    private String content_background_color;
    private String content_text_color;
    private String content_text_color_header;
    private String selected_text_color_header;
    private String site_text_color;

    public SiteSettings(JsonObject body) {
        this.input.put("body", body);
    }
}
