package com.nubeiot.edge.connector.bonescript;

import java.util.Map;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.rest.AbstractRestEventApi;
import com.nubeiot.core.http.rest.RestEventApi;

import io.vertx.core.http.HttpMethod;

public class BoneScriptRestEventApi extends AbstractRestEventApi {

    @Override
    protected void initRoute() {
        addRouter(BoneScriptEventBus.POINTS, "/point", "");
    }

    @Override
    protected Map<EventAction, HttpMethod> initHttpEventMap() {
        return RestEventApi.defaultEventHttpMap();
    }

}
