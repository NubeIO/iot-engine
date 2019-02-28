package com.nubeiot.edge.connector.driverapi;

import java.util.Map;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.rest.AbstractRestEventApi;
import com.nubeiot.core.http.rest.RestEventApi;

public class DriverRouteDefinitions extends AbstractRestEventApi {

    @Override
    protected void initRoute() {
        addRouter(DriverEventModels.POINTS, "/points", "");
    }

    @Override
    protected Map<EventAction, HttpMethod> initHttpEventMap() {
        return RestEventApi.defaultEventHttpMap();
    }

}
