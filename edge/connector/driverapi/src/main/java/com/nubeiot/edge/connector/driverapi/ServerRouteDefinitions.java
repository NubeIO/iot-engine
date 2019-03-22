package com.nubeiot.edge.connector.driverapi;

import com.nubeiot.core.http.rest.AbstractRestEventApi;
import com.nubeiot.edge.connector.driverapi.models.DriverEventModels;

public class ServerRouteDefinitions extends AbstractRestEventApi {

    @Override
    protected void initRoute() {
        addRouter(DriverEventModels.POINTS, "/points", "");
    }

    //    @Override
    //    protected Map<EventAction, HttpMethod> initHttpEventMap() {
    //        return RestEventApi.defaultEventHttpMap();
    //    }

}
