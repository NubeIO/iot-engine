package com.nubeiot.dashboard.impl.models;

import com.nubeiot.core.common.GeoPoint;
import com.nubeiot.core.common.Model;
import io.vertx.core.json.JsonObject;

public class Site extends Model {
    public String associated_company_id;
    public String logo_sm;
    public String logo_md;
    public String site_title;
    public String issue_address;
    public GeoPoint issue_location;
    public String site_footer;
    public String primary_color;
    public String secondary_color;
    public String text_color;
    public String text_color_secondary;
    public String heading_color;
    public String layout_body_background;
    public String btn_primary_bg;
    public String layout_header_background;
    public String rating_query;
    public String rating_label;
    public String role; // ADMIN or MANAGER or USER; defining the company level

    public Site(JsonObject body) {
        super(body);
    }
}
