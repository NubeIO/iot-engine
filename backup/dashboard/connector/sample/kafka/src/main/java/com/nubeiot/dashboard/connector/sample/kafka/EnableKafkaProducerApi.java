package com.nubeiot.dashboard.connector.sample.kafka;

import java.util.Collections;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.rest.AbstractRestEventApi;

public final class EnableKafkaProducerApi extends AbstractRestEventApi {

    @Override
    public EnableKafkaProducerApi initRouter() {
        addRouter(DashboardKafkaDemo.KAFKA_ENABLED, "/kafka/enable");
        return this;
    }

    @Override
    protected ActionMethodMapping initHttpEventMap() {
        return ActionMethodMapping.create(Collections.singletonMap(EventAction.UPDATE, HttpMethod.POST));
    }

}
