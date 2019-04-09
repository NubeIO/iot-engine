package com.nubeiot.edge.module.gateway;

import java.util.Collections;
import java.util.Map;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.rest.AbstractRestEventApi;

public class EdgeGatewayRestEventApi extends AbstractRestEventApi {

    @Override
    protected Map<EventAction, HttpMethod> initHttpEventMap() {
        return Collections.singletonMap(EventAction.GET_LIST, HttpMethod.GET);
    }

    @Override
    protected void initRoute() {
        this.addRouter(EdgeGatewayEventModel.infoModel, "/info");
    }

}
