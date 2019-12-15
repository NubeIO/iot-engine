package com.nubeiot.core.http.gateway;

import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.http.HttpServer;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.http.rest.AbstractRestEventApi;

public final class GatewayIndexApi extends AbstractRestEventApi {

    @Override
    public GatewayIndexApi initRouter() {
        addRouter(getSharedDataValue(HttpServer.SERVER_GATEWAY_ADDRESS_DATA_KEY), EventPattern.REQUEST_RESPONSE,
                  EventMethodDefinition.create("/index", "/:identifier", this));
        return this;
    }

    @Override
    protected ActionMethodMapping initHttpEventMap() {
        return ActionMethodMapping.READ_MAP;
    }

}
