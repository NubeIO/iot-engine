package com.nubeiot.edge.connector.bacnet.service.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ProtocolDispatcherMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointApiService;
import com.nubeiot.iotdata.edge.model.tables.pojos.ProtocolDispatcher;

import lombok.NonNull;

public class MockProtocolDispatcherService implements EventListener, EventHttpService {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public String api() {
        return DataPointApiService.DEFAULT.lookupApiName(ProtocolDispatcherMetadata.INSTANCE);
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        final ActionMethodMapping mapping = ActionMethodMapping.create(
            Collections.singletonMap(EventAction.CREATE_OR_UPDATE, HttpMethod.OTHER));
        return Collections.singleton(EventMethodDefinition.create("/dispatcher", "dispatcher_id", mapping));
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_ONE, EventAction.CREATE_OR_UPDATE);
    }

    @EventContractor(action = EventAction.CREATE_OR_UPDATE, returnType = Single.class)
    public Single<JsonObject> createOrUpdate(RequestData reqData) {
        final JsonObject resource = JsonPojo.from(
            new ProtocolDispatcher().fromJson(reqData.body()).setId(counter.incrementAndGet())).toJson();
        return Single.just(
            new JsonObject().put("action", EventAction.CREATE).put("status", Status.SUCCESS).put("resource", resource));
    }

}
