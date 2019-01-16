package com.nubeiot.dashboard.connector.sample.kafka;

import java.util.Collections;
import java.util.Map;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.rest.AbstractRestEventApi;

public class EnableKafkaProducerApi extends AbstractRestEventApi {

    @Override
    protected Map<EventAction, HttpMethod> initHttpEventMap() {
        return Collections.singletonMap(EventAction.UPDATE, HttpMethod.POST);
    }

    @Override
    protected void initRoute() {
        addRouter(DashboardKafkaDemo.KAFKA_ENABLED, "/kafka/enable");
    }

}
