package com.nubeiot.edge.module.datapoint.service;

import java.util.Collection;
import java.util.Set;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.http.OneToOneChildEntityHttpService;
import com.nubeiot.core.sql.service.AbstractOneToOneChildEntityService;
import com.nubeiot.core.sql.service.CreateOrUpdateEntityService;
import com.nubeiot.core.sql.service.OneToOneChildEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.HistorySettingMetadata;
import com.nubeiot.edge.module.datapoint.service.extension.PointExtension.PointOneToOneExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.HistorySetting;

import lombok.NonNull;

public final class HistorySettingService
    extends AbstractOneToOneChildEntityService<HistorySetting, HistorySettingMetadata>
    implements DataPointService<HistorySetting, HistorySettingMetadata>, PointOneToOneExtension,
               OneToOneChildEntityHttpService<HistorySetting, HistorySettingMetadata>,
               CreateOrUpdateEntityService<HistorySetting, HistorySettingMetadata> {

    public HistorySettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public HistorySettingMetadata context() {
        return HistorySettingMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return OneToOneChildEntityService.availableEvents(CreateOrUpdateEntityService.super.getAvailableEvents());
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return OneToOneChildEntityHttpService.super.definitions();
    }

    @EventContractor(action = EventAction.CREATE_OR_UPDATE, returnType = Single.class)
    public Single<JsonObject> createOrUpdate(RequestData requestData) {
        return CreateOrUpdateEntityService.super.createOrUpdate(requestData);
    }

}
