package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.sql.workflow.task.EntityTask;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ProtocolDispatcherMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.ProtocolDispatcher;

import lombok.NonNull;

public final class ProtocolDispatcherService
    extends AbstractEntityService<ProtocolDispatcher, ProtocolDispatcherMetadata>
    implements DataPointService<ProtocolDispatcher, ProtocolDispatcherMetadata> {

    public ProtocolDispatcherService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_ONE, EventAction.GET_LIST, EventAction.CREATE_OR_UPDATE);
    }

    @Override
    public EntityTask prePersistTask() {
        return null;
    }

    public Set<EventMethodDefinition> definitions() {
        //TODO temporary. microservice need to refactor to accept `null` HTTP Method a.k.a hide it in public mode
        final Map<EventAction, HttpMethod> mapping = new HashMap<>(ActionMethodMapping.DQL_MAP.get());
        mapping.put(EventAction.CREATE_OR_UPDATE, HttpMethod.OTHER);
        return EntityHttpService.createDefinitions(ActionMethodMapping.create(mapping), this::servicePath,
                                                   context()::requestKeyName, false);
    }

    @Override
    public ProtocolDispatcherMetadata context() {
        return ProtocolDispatcherMetadata.INSTANCE;
    }

    @EventContractor(action = EventAction.CREATE_OR_UPDATE, returnType = Single.class)
    public Single<JsonObject> createOrUpdate(RequestData requestData) {
        final ProtocolDispatcher req = context().onCreating(requestData);
        final ProtocolDispatcher filter = new ProtocolDispatcher().setAction(req.getAction())
                                                                  .setEntity(req.getEntity())
                                                                  .setProtocol(req.getProtocol());
        final RequestData reqFilter = RequestData.builder().filter(JsonPojo.from(filter).toJson()).build();
        return list(reqFilter).map(json -> json.getJsonArray(context().pluralKeyName()))
                              .filter(array -> !array.isEmpty())
                              .flatMapSingleElement(array -> patch(toUpdateRequest(req, array)))
                              .switchIfEmpty(create(requestData));
    }

    private RequestData toUpdateRequest(ProtocolDispatcher req, JsonArray array) {
        final ProtocolDispatcher db = context().parseFromRequest(array.getJsonObject(0));
        return RequestData.builder()
                          .body(JsonPojo.from(req).toJson().put(context().requestKeyName(), db.getId()))
                          .build();
    }

}
