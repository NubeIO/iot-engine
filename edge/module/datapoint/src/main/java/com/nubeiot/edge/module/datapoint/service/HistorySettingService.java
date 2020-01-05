package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.service.AbstractReferencingEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.HistorySettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.HistorySetting;

import lombok.NonNull;

public final class HistorySettingService
    extends AbstractReferencingEntityService<HistorySetting, HistorySettingMetadata>
    implements PointExtension, DataPointService<HistorySetting, HistorySettingMetadata> {

    public HistorySettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public HistorySettingMetadata context() {
        return HistorySettingMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_ONE, EventAction.CREATE_OR_UPDATE, EventAction.REMOVE);
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return PointExtension.oneToOneDefinitions(getAvailableEvents(), this::servicePath, context()::requestKeyName);
    }

    @EventContractor(action = EventAction.CREATE_OR_UPDATE, returnType = Single.class)
    public Single<JsonObject> createOrUpdate(RequestData reqData) {
        final HistorySetting data = context().onCreating(reqData);
        final RequestData creationData = PointExtension.createRequestData(reqData, JsonPojo.from(data).toJson());
        return create(creationData).onErrorResumeNext(patch(PointExtension.createRequestData(reqData, data.toJson())));
    }

}
