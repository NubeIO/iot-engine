package com.nubeiot.dashboard.props;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.dashboard.DashboardServerConfig;
import com.nubeiot.dashboard.Role;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderClassName = "Builder")
public class SiteProps {

    private final MongoClient mongoClient;
    private final MicroContext microContext;
    private final RoutingContext routingContext;
    private final DashboardServerConfig dashboardServerConfig;

    private JsonObject body;
    private JsonArray arrayBody;
    private JsonObject site;
    private JsonObject user;
    private String companyId;
    private String siteId;
    private String associatedCompanyId;
    private Role role;

}
