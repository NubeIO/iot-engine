package com.nubeiot.dashboard.props;

import io.github.zero.utils.Strings;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.dashboard.Role;

import lombok.Getter;

@Getter
public class DynamicCollectionProps {

    private final String collection;
    private final String siteId;
    private final JsonArray sitesIds;
    private final Role role;
    private final boolean isAuthorizedSiteId;

    public DynamicCollectionProps(RoutingContext ctx, String collection) {
        this.collection = collection;
        this.siteId = ctx.request().getHeader("Site-Id");
        JsonObject user = ctx.user().principal();
        this.sitesIds = extractSitesIds(user);
        this.role = Role.valueOf(user.getString("role"));
        this.isAuthorizedSiteId = sitesIds.contains(siteId);
    }

    private JsonArray extractSitesIds(JsonObject user) {
        JsonArray sitesIds = user.getJsonArray("sites_ids", new JsonArray());
        if (sitesIds.size() == 0 && Strings.isNotBlank(user.getString("site_id"))) {
            sitesIds = new JsonArray().add(user.getString("site_id"));
        }
        return sitesIds;
    }

}
