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
    public String role; // ADMIN or MANAGER or USER; defining the company level

    public Site(JsonObject body) {
        super(body);
    }
}
