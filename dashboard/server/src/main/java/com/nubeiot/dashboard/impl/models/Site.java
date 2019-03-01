package com.nubeiot.dashboard.impl.models;

import com.nubeiot.core.common.GeoPoint;
import com.nubeiot.core.common.Model;

import io.vertx.core.json.JsonObject;

public class Site extends Model {

    private String associated_company_id;
    private String logo_sm;
    private String logo_md;
    private String site_title;
    private String issue_address;
    private GeoPoint issue_location;
    private String site_footer;
    private String primary_color;
    private String secondary_color;
    private String text_color;
    private String text_color_secondary;
    private String heading_color;
    private String layout_body_background;
    private String btn_primary_bg;
    private String layout_header_background;
    private String rating_query;
    private String rating_label;
    private String role; // ADMIN or MANAGER or USER; defining the company level
    private String menu_layout;
    private String content_width;
    private Boolean fixed_sidebar;
    private Boolean fixed_header;
    private Boolean auto_hide_header;

    public Site(JsonObject body) {
        super(body);
    }

}
