package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.DeviceMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

import lombok.NonNull;

public final class DeviceService extends AbstractEntityService<Device, DeviceMetadata>
    implements DataPointService<Device, DeviceMetadata> {

    public DeviceService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public DeviceMetadata context() {
        return DeviceMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE, EventAction.PATCH);
    }

    @Override
    public @NonNull JsonObject afterPatch(Object key, @NonNull VertxPojo pojo, @NonNull RequestData reqData) {
        final JsonObject syncConfig = Optional.ofNullable(((Device) pojo).getMetadata())
                                              .map(info -> info.getJsonObject(DataSyncConfig.NAME, new JsonObject()))
                                              .orElse(new JsonObject());
        entityHandler().sharedData(DataPointIndex.DATA_SYNC_CFG, syncConfig);
        return enableFullResourceInCUDResponse()
               ? EntityTransformer.fullResponse(EventAction.PATCH, JsonPojo.from(pojo)
                                                                           .toJson(ignoreFields(reqData)))
               : EntityTransformer.keyResponse(resourceMetadata().requestKeyName(), key);
    }

    public interface DeviceExtension extends HasReferenceResource {

        @Override
        default EntityReferences entityReferences() {
            return new EntityReferences().add(DeviceMetadata.INSTANCE, "device");
        }

    }

}
