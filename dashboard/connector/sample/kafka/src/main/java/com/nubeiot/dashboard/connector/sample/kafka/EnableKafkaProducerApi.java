package com.nubeiot.dashboard.connector.sample.kafka;

import java.util.Collections;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.rest.AbstractRestEventApi;

public class EnableKafkaProducerApi extends AbstractRestEventApi {

    @Override
    protected void initRoute() {
        addRouter(DashboardKafkaDemo.KAFKA_ENABLED, "/kafka/enable");
    }

    @Override
    protected ActionMethodMapping initHttpEventMap() {
        return ActionMethodMapping.create(Collections.singletonMap(EventAction.UPDATE, HttpMethod.POST));
    }

}
